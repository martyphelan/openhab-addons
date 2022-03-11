package org.openhab.binding.wifiledcontroller.internal.handler;

public class WifiLedControllerCommunicationException extends Exception {
    private static final long serialVersionUID = 1L;

    public WifiLedControllerCommunicationException(WifiLedControllerHandler handler, Throwable cause) {
        super("Communication error. hostAddress: " + handler.hostAddress + " " + cause.getMessage(), cause);
    }
}
