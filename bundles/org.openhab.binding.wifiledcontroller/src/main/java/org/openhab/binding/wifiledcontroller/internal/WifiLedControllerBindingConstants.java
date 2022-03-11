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
package org.openhab.binding.wifiledcontroller.internal;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wifiledcontroller.internal.device.AK001_ZJ200_DeviceInfo;
import org.openhab.binding.wifiledcontroller.internal.device.DeviceInfo;
import org.openhab.binding.wifiledcontroller.internal.device.HF_A11_ZJ370_DeviceInfo;
import org.openhab.binding.wifiledcontroller.internal.device.HF_LPB100_ZJ002_DeviceInfo;
import org.openhab.binding.wifiledcontroller.internal.device.HF_LPB100_ZJ200_DeviceInfo;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WifiLedControllerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin T Phelan - Initial contribution
 */
@NonNullByDefault
public class WifiLedControllerBindingConstants {

    private static final String BINDING_ID = "wifiledcontroller";

    // Device Identifiers
    public final static String AK001_ZJ200 = "AK001-ZJ200";
    public final static String HF_LPB100_ZJ200 = "HF-LPB100-ZJ200";
    public final static String HF_LPB100_ZJ002 = "HF-LPB100-ZJ002";
    public final static String HF_A11_ZJ370 = "HF-A11-ZJ370";

    // ThingTypeUIDs
    public final static ThingTypeUID THING_TYPE_AK001_ZJ200 = new ThingTypeUID(BINDING_ID, AK001_ZJ200);
    public final static ThingTypeUID THING_TYPE_HF_LPB100_ZJ200 = new ThingTypeUID(BINDING_ID, HF_LPB100_ZJ200);
    public final static ThingTypeUID THING_TYPE_HF_LPB100_ZJ002 = new ThingTypeUID(BINDING_ID, HF_LPB100_ZJ002);
    public final static ThingTypeUID THING_TYPE_HF_A11_ZJ370 = new ThingTypeUID(BINDING_ID, HF_A11_ZJ370);

    // DeviceInfo Objects
    public static final DeviceInfo DEVICE_AK001_ZJ200 = new AK001_ZJ200_DeviceInfo();
    public static final DeviceInfo DEVICE_HF_LPB100_ZJ200 = new HF_LPB100_ZJ200_DeviceInfo();
    public static final DeviceInfo DEVICE_HF_LPB100_ZJ002 = new HF_LPB100_ZJ002_DeviceInfo();
    public static final DeviceInfo DEVICE_HF_A11_ZJ370 = new HF_A11_ZJ370_DeviceInfo();

    // Known ThingTypeUID's indexed by their Device Identifier
    public static final Map<String, ThingTypeUID> deviceThings = Stream
            .of(new Object[][] { { AK001_ZJ200, THING_TYPE_AK001_ZJ200 },
                    { HF_LPB100_ZJ200, THING_TYPE_HF_LPB100_ZJ200 }, { HF_LPB100_ZJ002, THING_TYPE_HF_LPB100_ZJ002 },
                    { HF_A11_ZJ370, THING_TYPE_HF_A11_ZJ370 }, })
            .collect(Collectors.toMap(data -> (String) data[0], data -> (ThingTypeUID) data[1]));

    // Known DeviceInfo's indexed by their Device Identifier
    public static final Map<String, DeviceInfo> deviceInfos = Stream
            .of(new Object[][] { { AK001_ZJ200, DEVICE_AK001_ZJ200 }, { HF_LPB100_ZJ200, DEVICE_HF_LPB100_ZJ200 },
                    { HF_LPB100_ZJ002, DEVICE_HF_LPB100_ZJ002 }, { HF_A11_ZJ370, DEVICE_HF_A11_ZJ370 }, })
            .collect(Collectors.toMap(data -> (String) data[0], data -> (DeviceInfo) data[1]));

    // ThingTypeUID's supported by this binding
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AK001_ZJ200,
            THING_TYPE_HF_LPB100_ZJ002, THING_TYPE_HF_LPB100_ZJ200, THING_TYPE_HF_A11_ZJ370);

    // List of all Channel ids
    public final static String POWER = "power";
    public final static String MASTER_LEVEL = "masterLevel";
    public final static String WHITE_LEVEL = "whiteLevel";
    public final static String COLOR = "color";
    public final static String MODE = "mode";
    public final static String SPEED = "speed";
    public final static String QUERY = "query";
    public final static String PRESET = "preset";
    public final static String STORE = "store";

    public static final String HOST_ADDRESS_PARAMETER = "hostAddress";
    public static final String MAC_ADDRESS_PARAMETER = "macAddress";

}
