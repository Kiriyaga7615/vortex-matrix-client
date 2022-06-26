package cis.matrixclient.util.render;

public class Color {
    public float hue, saturation, lightness;

    public int r, g, b, a;

    public Color(float hue, float saturation, float lightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;

        java.awt.Color c = java.awt.Color.getHSBColor(hue, saturation, lightness);
        this.r = c.getRed();
        this.g = c.getGreen();
        this.b = c.getBlue();
        this.a = 255;

        validateHSL();
    }

    public Color(float hue, float saturation, float lightness, int alpha) {
        this.hue = hue;
        this.saturation = saturation;
        this.lightness = lightness;

        java.awt.Color c = java.awt.Color.getHSBColor(hue, saturation, lightness);
        this.r = c.getRed();
        this.g = c.getGreen();
        this.b = c.getBlue();
        this.a = alpha;

        validateHSL();
    }

    public Color() {
        this(0f, 0f, 1f, 255);
    }

    public int getRgb() {
        java.awt.Color c = java.awt.Color.getHSBColor(hue, saturation, lightness);
        return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), a).getRGB();
    }

    public int getPacked(int a) {
        java.awt.Color color = java.awt.Color.getHSBColor(hue, saturation, lightness);
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return new java.awt.Color(r, g, b, a).getRGB();
    }

    public void setHue(float var) {hue = var;}
    public void setSaturation(float var) {saturation = var;}
    public void setLightness(float var) {lightness = var;}

    public float getHue() { return hue; }
    public float getSaturation() { return saturation; }
    public float getLightness() { return lightness; }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }
    public int getAlpha() { return a; }

    public void setRed(int value) { r = value;}
    public void setGreen(int value) { g = value;}
    public void setBlue(int value) { b = value;}
    public void setAlpha(int value) { a = value;}

    public Color alpha(int a){
        return new Color(r, g, b, a);
    }

    public Color copy() {
        return new Color(hue, saturation, lightness);
    }

    public void validateHSL() {
        if (hue < 0) hue = 0;
        if (hue > 360) hue = 360;

        if (saturation < 0) saturation = 0;
        if (saturation > 1) saturation = 1;

        if (lightness < 0) lightness = 0;
        if (lightness > 1) lightness = 1;
    }

    public void validateRGB(){
        if (r < 0) r = 0;
        else if (r > 255) r = 255;

        if (g < 0) g = 0;
        else if (g > 255) g = 255;

        if (b < 0) b = 0;
        else if (b > 255) b = 255;

        if (a < 0) a = 0;
        else if (a > 255) a = 255;
    }
}
