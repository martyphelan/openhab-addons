package org.openhab.binding.wifiledcontroller.internal.device;

public class HF_A11_ZJ370_DeviceInfo implements DeviceInfo {

    public HF_A11_ZJ370_DeviceInfo() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public byte[] getPowerCommand(boolean p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPowerResponseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getColorCommand(ColorState cs) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getColorResponseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getModeSpeedCommand(int mode, int speed) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getModeSpeedResponseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte[] getQueryCommand() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getQueryResponseLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getQueryPower(byte[] response) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getQueryMode(byte[] response) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getQuerySpeed(byte[] response) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void getQueryRGBW(ColorState colorState, byte[] response) {
        // TODO Auto-generated method stub

    }

}
