package org.openhab.binding.wifiledcontroller.internal.device;

import org.openhab.binding.wifiledcontroller.internal.util.ByteUtil;

public class HF_LPB100_ZJ200_DeviceInfo implements DeviceInfo {
    protected static final int SPEED_MIN = 31; // 0x1F

    @Override
    public byte[] getPowerCommand(boolean p) {
        return p ? ByteUtil.toBytesWithChecksum(0x71, 0x23, 0x0f) : ByteUtil.toBytesWithChecksum(0x71, 0x24, 0x0f);
    }

    @Override
    public int getPowerResponseLength() {
        return 0;
    }

    @Override
    public byte[] getColorCommand(ColorState cs) {
        return ByteUtil.toBytesWithChecksum(0x31, cs.getRed(), cs.getGreen(), cs.getBlue(), cs.getWhite(), 0, 0x0f);
    }

    @Override
    public int getColorResponseLength() {
        return 0;
    }

    @Override
    public byte[] getModeSpeedCommand(int mode, int speed) {
        int sp = speed * (SPEED_MIN - 1) / 100;
        sp = SPEED_MIN - sp;
        byte[] cmd = ByteUtil.toBytesWithChecksum(0x61, mode, sp, 0x0f);
        return cmd;
    }

    @Override
    public int getModeSpeedResponseLength() {
        return 0;
    }

    @Override
    public byte[] getQueryCommand() {
        return ByteUtil.toBytesWithChecksum(0x81, 0x8a, 0x8b);
    }

    @Override
    public int getQueryResponseLength() {
        return 14;
    }

    @Override
    public boolean getQueryPower(byte[] response) {
        return response[2] == (byte) 0x23;
    }

    @Override
    public int getQueryMode(byte[] response) {
        return Byte.toUnsignedInt(response[3]);
    }

    @Override
    public int getQuerySpeed(byte[] response) {
        int responseSpeed = Byte.toUnsignedInt(response[5]);
        int percent = ((responseSpeed - 1) * 100 / (SPEED_MIN - 1));
        int newSpeed = 100 - percent;
        // Fix range if invalid
        if (newSpeed > 100 || newSpeed < 0) {
            newSpeed = 0;
        }
        return newSpeed;
    }

    @Override
    public void getQueryRGBW(ColorState colorState, byte[] response) {
        colorState.setRGBW(response[6], response[7], response[8], response[9]);
    }
}
