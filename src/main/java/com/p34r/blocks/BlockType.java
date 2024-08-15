package com.p34r.blocks;

import org.joml.Vector2i;

public enum BlockType {
    AIR(0, 0),
    GRASS(0, 0),
    DIRT(1, 0);

    private Vector2i textCoordsOffset;

    private BlockType(int texX, int texY) {
        this.textCoordsOffset = new Vector2i(texX, texY);
    }

    public Vector2i getTextCoordsOffset() {
        return textCoordsOffset;
    }
}
