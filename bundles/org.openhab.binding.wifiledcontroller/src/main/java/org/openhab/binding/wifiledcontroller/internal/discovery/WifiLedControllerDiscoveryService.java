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
package org.openhab.binding.wifiledcontroller.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import org.openhab.binding.wifiledcontroller.internal.WifiLedControllerBindingConstants;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.wifiledcontroller")
public class WifiLedControllerDiscoveryService extends AbstractDiscoveryService {
    private static final String BROADCAST_ADDRESS = "255.255.255.255";
    // Discovery Constants
    private static final int DISCOVERY_PACKET_SIZE = 50;
    private static final int DISCOVERY_TIMEOUT = 5000;
    private static final int DISCOVERY_PORT = 48899;
    private static final byte[] DISCOVER_MESSAGE = "HF-A11ASSISTHREAD".getBytes();

    enum DiscoveryData {
        HOST_ADDRESS,
        MAC_ADDRESS,
        DEVICE_TYPE
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WifiLedControllerDiscoveryService() {
        super(WifiLedControllerBindingConstants.SUPPORTED_THING_TYPES_UIDS, 0, false);
    }

    @Override
    protected void startScan() {
        logger.debug("startScan");
        discover();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("stopScan");
        super.stopScan();
        // Remove anything older than NOW
        removeOlderResults(System.currentTimeMillis());
    }

    private void discover() {

        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setBroadcast(true);
            socket.setSoTimeout(DISCOVERY_TIMEOUT);
            // Send discovery message
            InetAddress group = InetAddress.getByName(BROADCAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(DISCOVER_MESSAGE, DISCOVER_MESSAGE.length, group,
                    DISCOVERY_PORT);
            socket.send(packet);

            // Listen for response(s)
            while (true) {
                byte[] buf = new byte[DISCOVERY_PACKET_SIZE];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String hostAddress = packet.getAddress().getHostName(); // Will be hostname or textual IP address
                int x = packet.getLength();
                if (x > 0) {
                    String data = new String(packet.getData(), 0, x);
                    String[] tokens = data.split(",");
                    if (tokens.length == DiscoveryData.values().length) {
                        // Identify device
                        ThingTypeUID thingTypeUID = WifiLedControllerBindingConstants.deviceThings
                                .get(tokens[DiscoveryData.DEVICE_TYPE.ordinal()]);
                        if (thingTypeUID != null) {
                            ThingUID thingUID = new ThingUID(thingTypeUID.getBindingId(), thingTypeUID.getId(),
                                    tokens[DiscoveryData.DEVICE_TYPE.ordinal()],
                                    tokens[DiscoveryData.MAC_ADDRESS.ordinal()]);
                            logger.debug("Found thing type. thingUID.toString: {} hostName: {}, ipAddress: {}",
                                    thingUID.getAsString(), hostAddress, tokens[DiscoveryData.HOST_ADDRESS.ordinal()]);

                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                    .withProperty(WifiLedControllerBindingConstants.HOST_ADDRESS_PARAMETER, hostAddress)
                                    .withProperty(WifiLedControllerBindingConstants.MAC_ADDRESS_PARAMETER,
                                            tokens[DiscoveryData.MAC_ADDRESS.ordinal()].replaceAll("(.{2})", "$1:")
                                                    .substring(0, 17))
                                    .withLabel(hostAddress).build();
                            thingDiscovered(discoveryResult);
                        } else {
                            logger.warn("Discovery of unknown device type. hostName: {}, data: {}", hostAddress, data);
                        }
                    } else {
                        logger.warn("Discovery data incomplete. Expected 3 tokens. hostName: {}, data: {}", hostAddress,
                                data);
                    }
                } else {
                    logger.warn("Discovery packet had no data. hostName: {}", hostAddress);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.debug("Discovery done.");
        } catch (IOException e) {
            logger.debug("Discovery ended.", e);
        }
    }

}
