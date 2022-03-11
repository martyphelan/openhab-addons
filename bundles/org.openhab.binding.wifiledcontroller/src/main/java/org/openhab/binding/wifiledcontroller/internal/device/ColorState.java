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
package org.openhab.binding.wifiledcontroller.internal.device;

import java.awt.Color;

import org.openhab.binding.wifiledcontroller.internal.util.ByteUtil;

/**
 * The {@link ColorState} class defines TODO
 *
 * @author Martin T Phelan - Initial contribution
 */
public class ColorState {
    // Color values 0-255
    private int red;
    private int green;
    private int blue;
    private int white;

    public ColorState() {
    }

    public ColorState(int red, int green, int blue, int white) {
        super();
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.white = white;
    }

    public ColorState(ColorState cs) {
        this.red = cs.red;
        this.green = cs.green;
        this.blue = cs.blue;
        this.white = cs.white;
    }

    public void setRGB(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setRGBW(int red, int green, int blue, int white) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.white = white;
    }

    /**
     * Sets the RGB levels and returns true if any changed
     *
     * @param red
     * @param green
     * @param blue
     * @return
     */
    public boolean setRGB(byte red, byte green, byte blue) {
        boolean b = false;
        b = this.red != red;
        this.red = ByteUtil.toInt(red);
        b = b || this.green != green;
        this.green = ByteUtil.toInt(green);
        b = b || this.blue != blue;
        this.blue = ByteUtil.toInt(blue);
        return b;
    }

    public void setRGB(int index, byte value) {
        switch (index) {
            case 0:
                this.red = ByteUtil.toInt(value);
                break;
            case 1:
                this.green = ByteUtil.toInt(value);
                break;
            case 2:
                this.blue = ByteUtil.toInt(value);
                break;
            default:
                throw new IllegalArgumentException("Invalid color index(0-2): " + index);
        }
    }

    public void setRGBW(byte red, byte green, byte blue, byte white) {
        this.red = ByteUtil.toInt(red);
        this.green = ByteUtil.toInt(green);
        this.blue = ByteUtil.toInt(blue);
        this.white = ByteUtil.toInt(white);
    }

    public void setColors(ColorState cs) {
        this.red = cs.red;
        this.green = cs.green;
        this.blue = cs.blue;
        this.white = cs.white;
    }

    /**
     * This will increment/decrement colors by 1 toward the target color state.
     *
     * @param cs
     */
    public void stepToward(ColorState cs) {
        this.red = stepToward(this.red, cs.red);
        this.green = stepToward(this.green, cs.green);
        this.blue = stepToward(this.blue, cs.blue);
        this.white = stepToward(this.white, cs.white);
    }

    public void transition(ColorState start, ColorState finish, int percent) {
        this.red = start.red + ((finish.red - start.red) * percent) / 100;
        this.green = start.green + ((finish.green - start.green) * percent) / 100;
        this.blue = start.blue + ((finish.blue - start.blue) * percent) / 100;
        this.white = start.white + ((finish.white - start.white) * percent) / 100;
    }

    private int stepToward(int currentLevel, int targetLevel) {
        int newLevel = currentLevel;
        if (targetLevel > currentLevel) {
            newLevel++;
        } else if (targetLevel < currentLevel) {
            newLevel--;
        }
        return newLevel;
    }

    public int getMaxDifference(ColorState cs) {
        int diff = Math.abs(this.red - cs.red);
        diff = Math.max(diff, Math.abs(this.green - cs.green));
        diff = Math.max(diff, Math.abs(this.blue - cs.blue));
        diff = Math.max(diff, Math.abs(this.white - cs.white));
        return diff;
    }

    /**
     * Get color as array of RGB bytes values 0-255
     *
     * @return color as array of RGB bytes values 0-255
     */
    public byte[] getRGB() {
        return new byte[] { (byte) red, (byte) green, (byte) blue };
    }

    public byte getRGB(int index) {
        switch (index) {
            case 0:
                return (byte) red;
            case 1:
                return (byte) green;
            case 2:
                return (byte) blue;
            default:
                throw new IllegalArgumentException("Invalid color index(0-2): " + index);
        }
    }

    /**
     * Get color as array of RGB bytes values 0-255
     *
     * @return color as array of RGB bytes values 0-255
     */
    public byte[] getRGBW() {
        return new byte[] { (byte) red, (byte) green, (byte) blue, (byte) white };
    }

    /**
     * Set the color using HSB values
     *
     * @param hue float value 0 to 360
     * @param saturation value 0 to 100
     * @param brightness value 0 to 100
     */
    public void setHSB(float hue, int saturation, int brightness) {
        Color color = new Color(
                Color.HSBtoRGB(hue / 360, (float) saturation / (float) 100, (float) brightness / (float) 100));
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    /**
     * Sets the given white level and returns true if changed
     *
     * @param white
     * @return
     */
    public boolean setWhite(byte white) {
        boolean b = white != this.white;
        this.white = ByteUtil.toInt(white);
        return b;
    }

    public int getWhite() {
        return white;
    }

    public void setWhiteLevel(int level) {
        white = (int) (255 * (level / (float) 100));
    }

    public int getWhiteLevel() {
        return (int) (100 * (white / (float) 255));
    }

    public void changeWhiteLevel(int percent) {
        white += (int) (255 * (percent / (float) 100));
        white = Math.min(white, 255);
    }

    public float getHue() {
        return Color.RGBtoHSB(red, green, blue, null)[0] * 360;
    }

    /**
     * Gets the saturation value of this color as percent 0-100
     *
     * @return
     */
    public int getSaturation() {
        return (int) (Color.RGBtoHSB(red, green, blue, null)[1] * 100);
    }

    /**
     * Gets the brightness value of this color as percent 0-100
     *
     * @return
     */
    public int getBrightness() {
        return (int) (Color.RGBtoHSB(red, green, blue, null)[2] * 100);
    }

    public void setBrightness(int percent) {
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        hsb[2] = percent / (float) 100;
        Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    public void changeBrightness(int percent) {
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        hsb[2] += percent / (float) 100;
        Color color = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
    }

    /**
     * Increment all colors by 1 up to 255 or given maximum
     *
     * @param max
     */
    public void incrementColors(ColorState max) {
        if (max == null) {
            max = new ColorState(255, 255, 255, 255);
        }
        red = Math.min(red + 1, max.red);
        green = Math.min(green + 1, max.green);
        blue = Math.min(blue + 1, max.blue);
        white = Math.min(white + 1, max.white);
    }

    /**
     * Decrement all colors by 1 down to 0 or given minimum
     *
     * @param max
     */
    public void decrementColors(ColorState min) {
        if (min == null) {
            min = new ColorState();
        }
        red = Math.max(red - 1, min.red);
        green = Math.max(green - 1, min.green);
        blue = Math.max(blue - 1, min.blue);
        white = Math.max(white - 1, min.white);
    }

    public void decrementColors() {
        decrementColors(null);
    }

    /**
     * Returns the value of the highest color level
     */
    public int getMaxColor() {
        int max = Math.max(red, green);
        max = Math.max(max, blue);
        max = Math.max(max, white);
        return max;
    }

    @Override
    public String toString() {
        return String.format("Color levels - red: %d, green %d, blue: %d, white: %d", red, green, blue, white);
    }

    public void reset() {
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.white = 0;
    }

    public static void main(String[] args) {
        ColorState current = new ColorState(255, 98, 0, 0);
        ColorState start = new ColorState(current);
        ColorState end = new ColorState(0, 200, 255, 0);
        current.transition(start, end, 100);
        System.out.println(current.toString());
        // Gson g = new Gson();
        // ColorState cs = g.fromJson("{}", ColorState.class);
        // System.out.println(cs.toString());
        // ColorState[] csa = g.fromJson("[{},{}]", ColorState[].class);
        // for (int i = 0; i < csa.length; i++) {
        // System.out.println(csa[i]);
        // }
        // String presets = null;
        // // presets = "[{red:15},{green:25}]";
        // ArrayList<ColorState> csal = g.fromJson(presets, new TypeToken<ArrayList<ColorState>>() {
        // }.getType());
        // if (csal != null) {
        // for (ColorState colorState : csal) {
        // System.out.println(colorState.toString());
        // }
        // }
        // ColorState cs = new ColorState();
        // cs.setRGB(0, 238, 255);
        // System.out.println("Hue=" + cs.getHue());
        // System.out.println("Saturation=" + cs.getSaturation());
        // System.out.println("Brightness=" + cs.getBrightness());
        // System.out.println("Bytes RGB=" + ByteUtil.bytesToHexString(cs.getRGB()));
        // cs.setHSB(184, 100, 100);
        // System.out.println("Hue=" + cs.getHue());
        // System.out.println("Saturation=" + cs.getSaturation());
        // System.out.println("Brightness=" + cs.getBrightness());
        // System.out.println("Bytes RGB=" + ByteUtil.bytesToHexString(cs.getRGB()));
        // cs.setWhiteLevel(100);
        // System.out.println("White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("White Level=" + cs.getWhiteLevel());
        // cs.setWhiteLevel(100);
        // cs.changeWhiteLevel(-25);
        // System.out.println("Decreased 25% White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("Decreased 25% White Level=" + cs.getWhiteLevel());
        // cs.changeWhiteLevel(25);
        // System.out.println("Increased 25% White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("Increased 25% White Level=" + cs.getWhiteLevel());
        // cs.setRGB(255, 255, 255);
        // cs.changeBrightness(-50);
        // System.out.println("Hue=" + cs.getHue());
        // System.out.println("Saturation=" + cs.getSaturation());
        // System.out.println("Brightness=" + cs.getBrightness());
        // System.out.println("Bytes RGB=" + ByteUtil.bytesToHexString(cs.getRGB()));
        // byte[] response = ByteUtil.toBytes(0x00, 0x00, 0x00, 0xbf);
        // cs.setRGBW(response[0], response[1], response[2], response[3]);
        // System.out.println("White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("White Level=" + cs.getWhiteLevel());
        // cs.white = ByteUtil.toInt((byte) 0xff);
        // System.out.println("White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("White Level=" + cs.getWhiteLevel());
        // cs.white = 255;
        // System.out.println("White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("White Level=" + cs.getWhiteLevel());
        // cs.white = ByteUtil.toInt((byte) 0xff);
        // System.out.println("White Byte=" + ByteUtil.toHex((byte) cs.white));
        // System.out.println("White Level=" + cs.getWhiteLevel());
    }
}
