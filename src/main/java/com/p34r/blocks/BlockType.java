package com.p34r.blocks;

public enum BlockType {
    AIR(0),
    DIRT(435);

    public final int color;

    private BlockType(int color) {
        this.color = color;
    }
}
