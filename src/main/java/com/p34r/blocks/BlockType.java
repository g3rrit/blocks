package com.p34r.blocks;

import org.joml.Vector2i;

public enum BlockType {
    AIR(0, 0, 0, 0, true),
    GRASS(1, 0, 0, 1, false),
    DIRT(2, 1, 0, 1, false),
    WATER(3, 2, 0, 1, true);

    private int idx;
    private Vector2i texCoordsOffset;
    private int texCount;
    private boolean transparent;

    private BlockType(int idx, int texX, int texY, int texCount, boolean transparent) {
        this.idx = idx;
        this.texCoordsOffset = new Vector2i(texX, texY);
        this.texCount = texCount;
        this.transparent = transparent;
    }

    public Vector2i getTexCoordsOffset() {
        return texCoordsOffset;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public int getIdx() {
        return idx;
    }
}
