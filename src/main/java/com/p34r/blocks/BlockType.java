package com.p34r.blocks;

import org.joml.Vector2i;

public enum BlockType {
    AIR(0, 0, 0, 0, true, BlockMaterial.AIR),
    GRASS(1, 0, 0, 1, false, BlockMaterial.SOLID),
    DIRT(2, 1, 0, 1, false, BlockMaterial.SOLID),
    WATER(3, 2, 0, 1, true, BlockMaterial.WATER),
    WOOD(4, 3, 0, 1, false, BlockMaterial.SOLID),
    LEAF(5, 4, 0, 1, false, BlockMaterial.SOLID);

    private int idx;
    private Vector2i texCoordsOffset;
    private int texCount;
    private boolean transparent;
    private BlockMaterial blockMaterial;

    private BlockType(int idx, int texX, int texY, int texCount, boolean transparent, BlockMaterial blockMaterial) {
        this.idx = idx;
        this.texCoordsOffset = new Vector2i(texX, texY);
        this.texCount = texCount;
        this.transparent = transparent;
        this.blockMaterial = blockMaterial;
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

    public BlockMaterial getBlockMaterial() {
        return blockMaterial;
    }
}
