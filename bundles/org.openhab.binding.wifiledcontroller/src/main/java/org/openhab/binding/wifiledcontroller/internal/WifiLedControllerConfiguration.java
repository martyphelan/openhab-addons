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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WifiLedControllerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Martin T Phelan - Initial contribution
 */
@NonNullByDefault
public class WifiLedControllerConfiguration {

    private String hostAddress = "";
    private String macAddress = "";
    private String presets = "";
    private String presetNames = "";
    private Boolean smoothTransition = false;

    public String getPresetNames() {
        return presetNames;
    }

    public void setPresetNames(String presetNames) {
        this.presetNames = presetNames;
    }

    public String getHostAddressParameter() {
        return hostAddress;
    }

    public void setHostAddressParameter(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getMacAddressParameter() {
        return macAddress;
    }

    public void setMacAddressParameter(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getPresets() {
        return presets;
    }

    public void setPresets(String presets) {
        this.presets = presets;
    }

    public Boolean getSmoothTransition() {
        return smoothTransition;
    }

    public void setSmoothTransition(Boolean smoothTransition) {
        this.smoothTransition = smoothTransition;
    }
}
