package net.onyx.client.utils;

import java.awt.Color;

public class ColorUtils {

    public static String colorChar = "\247";
    public static String purple = "\2475";
    public static String red = "\247c";
    public static String aqua = "\247b";
    public static String green = "\247a";
    public static String blue = "\2479";
    public static String darkGreen = "\2472";
    public static String darkBlue = "\2471";
    public static String black = "\2470";
    public static String darkRed = "\2474";
    public static String darkAqua = "\2473";
    public static String lightPurple = "\247d";
    public static String yellow = "\247e";
    public static String white = "\247f";
    public static String gray = "\2477";
    public static String darkGray = "\2478";
    public static String reset = "\247r";
    public static String pingle = "#FF1464";
    public static int defaultClientColor = new java.awt.Color(140, 0, 0).getRGB();


    public static java.awt.Color defaultClientColor() {
        return new java.awt.Color(140, 0, 0);
    }

    public static int rainbow(float seconds, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (int) (seconds * 1000)) / (seconds * 1000);
        int color = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return color;
    }

    public static int rainbow(float seconds, float saturation, float brigtness, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int) (seconds * 1000)) / (seconds * 1000);
        int color = java.awt.Color.HSBtoRGB(hue, saturation, 1);
        return color;
    }

    public static java.awt.Color fade(java.awt.Color color, int index, int count) {
        float[] hsb = new float[3];
        java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)index / (float)count * 2.0F) % 2.0F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        hsb[2] = brightness % 2.0F;
        return new java.awt.Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static java.awt.Color fade(java.awt.Color color, java.awt.Color color2, int index, int count) {
        float[] hsb = new float[3];
//	      System.out.println((((float)(System.currentTimeMillis() % 2000L) / 1000.0F)) <= 1);
//	      if ((((float)(System.currentTimeMillis() % 2000L) / 1000.0F)) <= 1)
        java.awt.Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
//	      else Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() + index % 2000L) / 1000.0F + (float)index / (float)count * 2.0F) % 2.0F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        hsb[2] = brightness % 2.0F;
        System.out.println(hsb[2]);
        return new java.awt.Color(java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static java.awt.Color getColor(int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        return new java.awt.Color(red, green, blue, alpha);
    }

    public static int transparent(int rgb, int opacity) {
        java.awt.Color color = new java.awt.Color(rgb);
        return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), opacity).getRGB();
    }

    public static int transparent(int opacity) {
        java.awt.Color color = java.awt.Color.BLACK;
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity).getRGB();
    }
}
