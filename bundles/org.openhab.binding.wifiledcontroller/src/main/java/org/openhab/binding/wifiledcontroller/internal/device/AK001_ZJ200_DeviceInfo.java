package org.openhab.binding.wifiledcontroller.internal.device;

public class AK001_ZJ200_DeviceInfo extends HF_LPB100_ZJ200_DeviceInfo {

    @Override
    public int getColorResponseLength() {
        return 1;
    }

    @Override
    public int getPowerResponseLength() {
        return 4;
    }

}
