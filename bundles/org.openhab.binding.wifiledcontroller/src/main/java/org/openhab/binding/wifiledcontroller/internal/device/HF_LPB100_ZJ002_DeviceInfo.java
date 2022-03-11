package org.openhab.binding.wifiledcontroller.internal.device;

import org.openhab.binding.wifiledcontroller.internal.util.ByteUtil;

public class HF_LPB100_ZJ002_DeviceInfo extends HF_LPB100_ZJ200_DeviceInfo {

    @Override
    public byte[] getPowerCommand(boolean p) {
        return p ? ByteUtil.toBytesWithChecksum(0x71, 0x23) : ByteUtil.toBytesWithChecksum(0x71, 0x24);
    }

    @Override
    public int getPowerResponseLength() {
        return 3;
    }

    @Override
    public byte[] getColorCommand(ColorState cs) {
        return ByteUtil.toBytesWithChecksum(0x31, cs.getRed(), cs.getGreen(), cs.getBlue(), cs.getWhite(), 0, 0);
    }

    @Override
    public byte[] getModeSpeedCommand(int mode, int speed) {
        int sp = speed * (SPEED_MIN - 1) / 100;
        sp = SPEED_MIN - sp;
        byte[] cmd = ByteUtil.toBytesWithChecksum(0x61, mode, sp);
        return cmd;
    }

}
