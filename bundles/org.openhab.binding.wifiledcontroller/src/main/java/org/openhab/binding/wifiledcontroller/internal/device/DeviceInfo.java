package org.openhab.binding.wifiledcontroller.internal.device;

public interface DeviceInfo {

    public abstract byte[] getPowerCommand(boolean p);

    public abstract int getPowerResponseLength();

    public abstract byte[] getColorCommand(ColorState cs);

    public abstract int getColorResponseLength();

    public abstract byte[] getModeSpeedCommand(int mode, int speed);

    public abstract int getModeSpeedResponseLength();

    public abstract byte[] getQueryCommand();

    public abstract int getQueryResponseLength();

    public abstract boolean getQueryPower(byte[] response);

    public abstract int getQueryMode(byte[] response);

    public abstract int getQuerySpeed(byte[] response);

    public abstract void getQueryRGBW(ColorState colorState, byte[] response);

}
