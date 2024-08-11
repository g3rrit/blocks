package com.p34r.blocks;

import org.joml.Vector3f;

public class Side {
    public static final int FRONT = 0;
    public static final int BACK = 1;
    public static final int TOP = 2;
    public static final int BOTTOM = 3;
    public static final int RIGHT = 4;
    public static final int LEFT = 5;

    private static final Vector3f[] normals = new Vector3f[]{
        new Vector3f(0, 0, 1),
        new Vector3f(0, 0, -1),
        new Vector3f(0, 1, 0),
        new Vector3f(0, -1, 0),
        new Vector3f(1, 0, 0),
        new Vector3f(-1, 0, 0),
    };

    public static Vector3f getNormal(int side) {
        return normals[side];
    }
}
