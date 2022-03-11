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
package org.openhab.binding.wifiledcontroller.internal.util;

/**
 * The {@link ByteUtil} class defines TODO
 *
 * @author Martin T Phelan - Initial contribution
 */
public class ByteUtil {

    public static byte[] toBytes(int... ints) { // helper function
        byte[] result = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte) ints[i];
        }
        return result;
    }

    public static byte[] toBytesWithChecksum(int... ints) { // helper function
        byte[] result = new byte[ints.length + 1];
        for (int i = 0; i < ints.length; i++) {
            result[i] = (byte) ints[i];
        }
        result[ints.length] = checkSum(result);
        return result;
    }

    public static final byte checkSum(byte[] bytes) {
        byte sum = 0;
        for (byte b : bytes) {
            // sum ^= b;
            sum += b;
        }
        sum = (byte) (sum & 255);
        return sum;
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes != null) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(toHex(b));
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    public static String toHex(byte b) {
        return String.format("%02x ", b & 0xff);
    }

    public static int toInt(byte b) {
        return b & 0xff;
    }
}
