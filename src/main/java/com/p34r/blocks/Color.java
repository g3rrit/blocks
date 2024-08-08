package com.p34r.blocks;

public class Color {
    public float r;
    public float g;
    public float b;
    public float a;

    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1.0f;
    }

    public Color(float r, float g, float b, float a) {
        this(r, g, b);
        this.a = a;
    }
}
