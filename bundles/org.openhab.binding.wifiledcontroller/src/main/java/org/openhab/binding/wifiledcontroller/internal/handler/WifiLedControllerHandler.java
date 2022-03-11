/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.wifiledcontroller.internal.handler;

import static org.openhab.binding.wifiledcontroller.internal.WifiLedControllerBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wifiledcontroller.internal.WifiLedControllerConfiguration;
import org.openhab.binding.wifiledcontroller.internal.WifiLedControllerDynamicStateDescriptionProvider;
import org.openhab.binding.wifiledcontroller.internal.device.ColorState;
import org.openhab.binding.wifiledcontroller.internal.device.DeviceInfo;
import org.openhab.binding.wifiledcontroller.internal.util.ByteUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link WifiLedControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin T Phelan - Initial contribution
 */
@NonNullByDefault
public class WifiLedControllerHandler extends BaseThingHandler {
    // Common Controller Information
    private static final String PRESET_NONE = "-";
    private static final int COMMAND_PORT = 5577;
    private static final int MODE_COLOR = 0x61;
    // Configuration Information
    @Nullable
    protected String hostAddress;
    @Nullable
    Socket socket;
    private int timeout = 3000;
    protected HashMap<String, ColorState> presets = new HashMap<String, ColorState>();;
    private int increaseDecreaseLevel = 5;
    protected boolean smoothTransition = false;
    // State information
    protected boolean disposed = false;
    protected boolean power;
    protected ColorState colorState = new ColorState();
    protected String preset = PRESET_NONE;
    protected int mode = MODE_COLOR; // 0 - max
    protected int speed = 50; // 0 - 100
    protected final Logger logger = LoggerFactory.getLogger(WifiLedControllerHandler.class);
    protected final WifiLedControllerDynamicStateDescriptionProvider provider;
    protected final DeviceInfo device;
    // Recovery
    private static final int RETRY_MAX_COUNT = 10;
    private static final int RETRY_DELAY_MS = 10000;
    private int retries = RETRY_MAX_COUNT;

    // =================================================================================
    // Constructors
    // =================================================================================

    public WifiLedControllerHandler(Thing thing, WifiLedControllerDynamicStateDescriptionProvider provider,
            DeviceInfo device) {
        super(thing);
        this.provider = provider;
        this.device = device;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    // =================================================================================
    // Binding Core Routines
    // =================================================================================

    @Override
    public void initialize() {
        if (disposed) {
            return;
        }
        WifiLedControllerConfiguration thingConfig = getConfigAs(WifiLedControllerConfiguration.class);
        hostAddress = thingConfig.getHostAddressParameter();
        setPresetsFromJSON(thingConfig.getPresets());
        updateState(PRESET, new StringType(PRESET_NONE));
        String presetNames = thingConfig.getPresetNames();
        smoothTransition = thingConfig.getSmoothTransition();
        updatePresetOptions(presetNames);
        scheduler.execute(new Runnable() {
            @Override
            public void run() {
                logger.debug("Invoking Job queryState. hostAddress: {}", hostAddress);
                try {
                    queryDeviceState();
                    updateStatus(ThingStatus.ONLINE);
                    closeSocket();
                } catch (WifiLedControllerCommunicationException e) {
                    closeSocket();
                    handleQueryDeviceFailure(e);
                }
            }
        });
        logger.debug("Initialize complete. thingUID: {}, hostAddress: {}", thing.getUID().getAsString(), hostAddress);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (disposed) {
            return;
        }
        try {
            if (channelUID.getId().equals(POWER)) {
                handlePowerCommand(command.toString().equals("ON"));
            } else if (channelUID.getId().equals(QUERY)) {
                handleQueryDeviceState(command.toString().equals("ON"));
            } else if (channelUID.getId().equals(COLOR)) {
                handleColorCommand(command);
            } else if (channelUID.getId().equals(PRESET)) {
                handlePresetChangeCommand(command);
            } else if (channelUID.getId().equals(STORE)) {
                handleStorePresetCommand(command.toString().equals("ON"));
            } else if (channelUID.getId().equals(WHITE_LEVEL)) {
                handleWhiteLevelCommand(command);
            } else if (channelUID.getId().equals(MODE)) {
                handleModeCommand(command);
            } else if (channelUID.getId().equals(SPEED)) {
                handleSpeedCommand(command);
            } else {
                logger.error("handleCommand unknown command error. hostAddress: {}, channelUID: {}, command: {}",
                        hostAddress, channelUID.getAsString(), command.toString());
                closeSocket();
                return; // Do not update Thing Status
            }
            // Update thing status if needed
            if (getThing().getStatus() == ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.ONLINE);
                retries = RETRY_MAX_COUNT;
            }
            closeSocket();
        } catch (WifiLedControllerCommunicationException e) {
            closeSocket();
            handleCommandFailure(e, channelUID, command);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        String presetNames = (String) configuration.get("presetNames");
        smoothTransition = (Boolean) configuration.get("smoothTransition");
        updatePresetOptions(presetNames);
        logger.debug("Configuration has been updated!! presetNames: {}, smoothTransition: {}", presetNames,
                smoothTransition);
    }

    private void updatePresetOptions(String presetNames) {
        List<StateOption> options = new ArrayList<StateOption>();
        options.add(new StateOption(PRESET_NONE, "NONE"));
        String[] newOptions = presetNames.split(",");
        for (String newOption : newOptions) {
            newOption = newOption.trim();
            options.add(new StateOption(newOption, newOption));
        }
        provider.setStateOptions(new ChannelUID(getThing().getUID(), PRESET), options);
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("thingUpdated invoked.");
        super.thingUpdated(thing);
    }

    // =================================================================================
    // Recovery Methods
    // =================================================================================

    private void handleCommandFailure(Exception e, ChannelUID channelUID, Command command) {
        if (disposed) {
            return;
        }
        if (retries > 0) {
            retries--;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            logger.error(
                    "Command failure - retrying. retries left: {} thingUID: {}, hostAddress: {}, error message: {}",
                    retries, thing.getUID().getAsString(), hostAddress, e.getMessage());
            scheduler.schedule(new Runnable() {

                @Override
                public void run() {
                    handleCommand(channelUID, command);
                }
            }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
        } else {
            // switch recovery strategy
            logger.error("No more retries left for handleCommandFailure. thingUID: {}, hostAddress: {}",
                    thing.getUID().getAsString(), hostAddress);
            handleQueryDeviceFailure(e);
        }
    }

    private void handleQueryDeviceFailure(Exception e) {
        if (disposed) {
            return;
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        logger.error("Query Device failure - retrying. thingUID: {}, hostAddress: {}, error message: {}",
                thing.getUID().getAsString(), hostAddress, e.getMessage());
        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    queryDeviceState();
                    updateStatus(ThingStatus.ONLINE);
                    retries = RETRY_MAX_COUNT;
                    closeSocket();
                } catch (WifiLedControllerCommunicationException e) {
                    closeSocket();
                    handleQueryDeviceFailure(e);
                }
            }
        }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    // =================================================================================
    // Power Methods
    // =================================================================================

    private void handlePowerCommand(boolean onOff) throws WifiLedControllerCommunicationException {
        // Determine if fade on/off or just ON/OFF
        if (smoothTransition && mode == MODE_COLOR) {
            if (onOff) {
                fadeOn();
            } else {
                fadeOff();
            }
        } else {
            sendPowerCommand(onOff);
        }
    }

    private void sendPowerCommand(boolean onOff) throws WifiLedControllerCommunicationException {
        sendReceive(device.getPowerCommand(onOff), device.getPowerResponseLength());
        updatePowerState(onOff);
    }

    private void updatePowerState(boolean onOff) {
        // Update internal state
        power = onOff;
        // Update framework state
        OnOffType t = power ? OnOffType.ON : OnOffType.OFF;
        updateState(POWER, t);
        // If powering OFF, set preset to NONE (-1)
        if (!power) {
            preset = PRESET_NONE;
            updateState(PRESET, new StringType(PRESET_NONE));
        }
    }

    // =================================================================================
    // Fade On/Off Methods
    // =================================================================================

    private void transition(ColorState finish) throws WifiLedControllerCommunicationException {
        ColorState start = new ColorState(colorState);
        int transitionMs = 2000;
        int percentIncrement = 4;
        int delayMs = transitionMs / (100 / percentIncrement);
        int percent = smoothTransition ? 0 : 100 - percentIncrement;
        while (percent < 100) {
            percent += percentIncrement;
            colorState.transition(start, finish, percent);
            sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
            if (percent < 100) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void fadeOn() throws WifiLedControllerCommunicationException {
        ColorState finish = new ColorState(colorState);
        // Set all levels at 0
        colorState.setRGBW(0, 0, 0, 0);
        // Set color state but do not framework state
        // we are NOT ultimately changing color state
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        // send command and update state
        sendPowerCommand(true);
        transition(finish);
        logger.debug("fadeOn Complete. hostAddress: {}", hostAddress);
    }

    private void fadeOff() throws WifiLedControllerCommunicationException {
        ColorState colorStateSave = new ColorState(colorState);
        ColorState finish = new ColorState();
        transition(finish);
        // send command and update state
        sendPowerCommand(false);
        // Restore color levels
        colorState.setColors(colorStateSave);
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        logger.debug("fadeOff complete. hostAddress: {}, restoring saved colors: red: {}, green: {}, blue: {}",
                hostAddress, colorState.getRed(), colorState.getGreen(), colorState.getBlue());
    }

    // =================================================================================
    // Color Methods
    // =================================================================================

    private void handleColorCommand(Command command) throws WifiLedControllerCommunicationException {
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            setHSBLevel(hsb.getHue().floatValue(), hsb.getSaturation().intValue(), hsb.getBrightness().intValue());
        } else if (command instanceof PercentType) {
            PercentType percent = (PercentType) command;
            setBrightness(percent.intValue());
        } else if (command instanceof OnOffType) {
            setBrightness(command.toString().equals("ON") ? 100 : 0);
        } else if (command instanceof IncreaseDecreaseType) {
            if (command.toString().equals("INCREASE")) {
                increaseBrightness();
            } else {
                decreaseBrightness();
            }
        } else {
            logger.error("handleColorCommand unknown color command. hostAddress: {}, command: {}", hostAddress,
                    command.getClass().getName());
        }
    }

    private void setHSBLevel(float hue, int saturation, int brightness) throws WifiLedControllerCommunicationException {
        colorState.setHSB(hue, saturation, brightness);
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        updateStateColor();
    }

    private void setBrightness(int percent) throws WifiLedControllerCommunicationException {
        colorState.setBrightness(percent);
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        updateStateColor();
    }

    private void increaseBrightness() throws WifiLedControllerCommunicationException {
        colorState.changeBrightness(increaseDecreaseLevel);
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        updateStateColor();
    }

    private void decreaseBrightness() throws WifiLedControllerCommunicationException {
        colorState.changeBrightness(increaseDecreaseLevel);
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        updateStateColor();
    }

    private void updateStateColor() {
        logger.debug("updateStateColor colorState: {}", colorState.toString());
        DecimalType h = new DecimalType(colorState.getHue());
        PercentType s = new PercentType(colorState.getSaturation());
        PercentType b = new PercentType(colorState.getBrightness());
        updateState(COLOR, new HSBType(h, s, b));
        if (mode != MODE_COLOR) {
            mode = MODE_COLOR;
            updateState(MODE, new DecimalType(mode));
        }
    }

    // =================================================================================
    // White Level Methods
    // =================================================================================

    private void handleWhiteLevelCommand(Command command) throws WifiLedControllerCommunicationException {
        if (command.toString().equals("INCREASE")) {
            increaseWhiteLevel();
        } else if (command.toString().equals("DECREASE")) {
            decreaseWhiteLevel();
        } else if (command instanceof PercentType) {
            setWhiteLevel(((PercentType) command).intValue());
        } else if (command instanceof OnOffType) {
            setWhiteLevel(command.toString().equals("ON") ? 100 : 0);
        } else {
            logger.error("handleColorCommand - Unknown white level command. hostAddress: {}, command: {}", hostAddress,
                    command.getClass().getName());
        }
    }

    private void setWhiteLevel(int level) throws WifiLedControllerCommunicationException {
        colorState.setWhiteLevel(level);
        updateStateWhite();
    }

    private void increaseWhiteLevel() throws WifiLedControllerCommunicationException {
        colorState.changeWhiteLevel(increaseDecreaseLevel);
        updateStateWhite();
    }

    private void decreaseWhiteLevel() throws WifiLedControllerCommunicationException {
        colorState.changeWhiteLevel(increaseDecreaseLevel);
        updateStateWhite();
    }

    private void updateStateWhite() throws WifiLedControllerCommunicationException {
        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
        updateState(WHITE_LEVEL, new PercentType(colorState.getWhiteLevel()));
        if (mode != MODE_COLOR) {
            mode = MODE_COLOR;
            updateState(MODE, new DecimalType(mode));
        }
    }

    // =================================================================================
    // Speed Methods
    // =================================================================================

    private void handleSpeedCommand(Command command) throws WifiLedControllerCommunicationException {
        if (command.toString().equals("INCREASE")) {
            increaseSpeed();
        } else if (command.toString().equals("DECREASE")) {
            decreaseSpeed();
        } else if (command instanceof PercentType) {
            setSpeed(((PercentType) command).intValue());
        } else if (command instanceof OnOffType) {
            setSpeed(command.toString().equals("ON") ? 100 : 0);
        } else {
            logger.error("handleColorCommand - Unknown white level command. hostAddress: {}, command: {}", hostAddress,
                    command.getClass().getName());
        }
    }

    private void setSpeed(int level) throws WifiLedControllerCommunicationException {
        speed = level;
        sendReceive(device.getModeSpeedCommand(mode, speed), device.getModeSpeedResponseLength());
        updateState(SPEED, new PercentType(speed));
    }

    private void increaseSpeed() throws WifiLedControllerCommunicationException {
        speed = speed + increaseDecreaseLevel;
        speed = Math.min(speed, 100);
        sendReceive(device.getModeSpeedCommand(mode, speed), device.getModeSpeedResponseLength());
        updateState(SPEED, new PercentType(speed));
    }

    private void decreaseSpeed() throws WifiLedControllerCommunicationException {
        speed = speed - increaseDecreaseLevel;
        speed = Math.max(speed, 0);
        sendReceive(device.getModeSpeedCommand(mode, speed), device.getModeSpeedResponseLength());
        updateState(SPEED, new PercentType(speed));
    }

    // =================================================================================
    // Mode Methods
    // =================================================================================

    private void handleModeCommand(Command command) throws WifiLedControllerCommunicationException {
        logger.debug("handlePresetCommand begin. hostAddress: {}, command: {}", hostAddress, command.toString());
        if (command instanceof DecimalType) {
            mode = ((DecimalType) command).intValue();
            sendReceive(device.getModeSpeedCommand(mode, speed), device.getModeSpeedResponseLength());
            updateState(MODE, new DecimalType(mode));
            // If transitioned back to Color mode, restore device current colors
            if (mode == MODE_COLOR) {
                sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
            }
        } else {
            logger.error("handlePresetCommand unknown command type. hostAddress: {}, command: {}", hostAddress,
                    command.toString());
        }
    }

    // =================================================================================
    // Query Device State Methods
    // =================================================================================

    private void handleQueryDeviceState(boolean onOff) throws WifiLedControllerCommunicationException {
        if (onOff) {
            // Start the status checker
            updateState(QUERY, OnOffType.ON);
            queryDeviceState();
            updateState(QUERY, OnOffType.OFF);
        } else {
            updateState(QUERY, OnOffType.OFF);
        }
    }

    private void queryDeviceState() throws WifiLedControllerCommunicationException {
        byte[] response = sendReceive(device.getQueryCommand(), device.getQueryResponseLength());
        // Power state
        updatePowerState(device.getQueryPower(response));
        // Update RGB & White state
        device.getQueryRGBW(colorState, response);
        updateState(WHITE_LEVEL, new PercentType(colorState.getWhiteLevel()));
        updateStateColor();
        // Mode state
        mode = device.getQueryMode(response);
        updateState(MODE, new DecimalType(mode));
        // Speed State
        speed = device.getQuerySpeed(response);
        updateState(SPEED, new PercentType(speed));
    }

    // =================================================================================
    // Preset Store Methods
    // =================================================================================

    /**
     * Store currently selected preset colors. This uses the current value of preset to indicate
     * which preset to store.
     *
     * @param onOff
     */
    private void handleStorePresetCommand(boolean onOff) {
        if (onOff) {
            updateState(STORE, OnOffType.ON);
            logger.debug("handleStorePresetCommand: preset: {}", preset);
            if (!PRESET_NONE.equals(preset)) {
                presets.put(preset, new ColorState(colorState));
                getConfig().put("presets", getPresetsAsJSON());
                updateConfiguration(getConfig());
                logger.debug("handleStorePresetCommand hostAddress: {} preset: {}, presets: {}", hostAddress, preset,
                        getConfig().get("presets"));
            } else {
                logger.debug("handleStorePresetCommand ignored. hostAddress: {}, preset: {}", hostAddress, preset);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            updateState(STORE, OnOffType.OFF);
        } else {
            updateState(STORE, OnOffType.OFF);
        }
    }

    private void setPresetsFromJSON(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, ColorState>>() {
        }.getType();
        try {
            HashMap<String, ColorState> temp = gson.fromJson(json, type);
            if (temp != null) {
                presets = temp;
            }
        } catch (Exception e) {
            logger.error("setPresetsFromJSON Error while parsing JSON preset values. hostAddress: {}", hostAddress, e);
        }
    }

    private String getPresetsAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(presets);
    }

    // =================================================================================
    // Preset Change Methods
    // =================================================================================

    private void handlePresetChangeCommand(Command command) throws WifiLedControllerCommunicationException {
        if (command instanceof StringType) {
            String newPreset = ((StringType) command).toString();
            if (!newPreset.equals(preset)) {
                logger.debug("handlePresetCommand changing value. hostAddress: {}, newPreset: {}", hostAddress,
                        newPreset);
                preset = newPreset;
                updateState(PRESET, new StringType(preset));
                // If preset exists then transition
                if (presets.containsKey(preset)) {
                    ColorState colorStateNew = new ColorState(presets.get(preset));
                    if (!power) {
                        colorState.reset();
                        sendReceive(device.getColorCommand(colorState), device.getColorResponseLength());
                        sendPowerCommand(true);
                    }
                    transition(colorStateNew);
                    updateStateColor();
                    updateState(WHITE_LEVEL, new PercentType(colorState.getWhiteLevel()));
                    if (mode != MODE_COLOR) {
                        mode = MODE_COLOR;
                        updateState(MODE, new DecimalType(mode));
                    }
                }
            }
        }
    }

    // =================================================================================
    // Utility Communication Methods
    // =================================================================================

    @SuppressWarnings("null")
    private void openSocket() throws WifiLedControllerCommunicationException {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(hostAddress, COMMAND_PORT);
            }
        } catch (IOException e) {
            logger.error("openSocket ERROR. hostAddress: {}, msg: {}", hostAddress, e.getMessage());
            socket = null;
            throw new WifiLedControllerCommunicationException(this, e);
        }
    }

    @SuppressWarnings("null")
    private void closeSocket() {
        if (socket != null) {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
                socket = null;
            } catch (IOException e) {
                logger.error("closeSocket ERROR. hostAddress: {}, msg: {}", hostAddress, e.getMessage());
                socket = null;
            }
        }
    }

    @SuppressWarnings("null")
    private byte[] sendReceive(byte[] cmd, int responseSize) throws WifiLedControllerCommunicationException {
        openSocket();
        try (/* Socket mySocket = new Socket(hostAddress, COMMAND_PORT); */
                OutputStream myCommandStream = socket.getOutputStream();
                InputStream myResponseStream = socket.getInputStream()) {
            socket.setSoTimeout(timeout);
            myCommandStream.write(cmd);
            byte[] response = myResponseStream.readNBytes(responseSize);
            logger.debug("sendReceive. hostAddress: {}, command: {}, response: {}", hostAddress,
                    ByteUtil.bytesToHexString(cmd), ByteUtil.bytesToHexString(response));
            return response;
        } catch (IOException e) {
            logger.error("sendReceive ERROR. hostAddress: {}, command: {}, expect responseSize: {}, msg: {}",
                    hostAddress, ByteUtil.bytesToHexString(cmd), responseSize, e.getMessage());
            throw new WifiLedControllerCommunicationException(this, e);
        }
    }
}
