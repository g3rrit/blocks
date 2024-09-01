package com.p34r.blocks;

import org.joml.Vector2i;
import org.lwjgl.bgfx.BGFXCacheReadSizeCallback;

import java.util.Arrays;
import java.util.Objects;

public class BlockGrid {
    public static final int TEXTURE_SIZE = 64;
    public static final int BLOCK_COUNT = 8;
    public static final float TEXTURE_DIV = (float) BLOCK_COUNT / TEXTURE_SIZE;

    public class Grid {
        public boolean empty = true;
        public float[] vertices = null;
        public int[] indices = null;
        public float[] texCoords = null;
    }

    private BlockType blockType;

    /**
     * for top/bottom/etc.
     */
    private final Grid[] grids = new Grid[]{
        new Grid(),
        new Grid(),
        new Grid(),
        new Grid(),
        new Grid(),
        new Grid()
    };

    private int activeFaces = 0;

    /**
     * Generates the temporary mesh of the current block
     *
     * Faces:
     * 0 - front
     * 1 - back
     * 2 - top
     * 3 - bottom
     * 4 - right
     * 5 - left
     */
    public BlockGrid(BlockType blockType, int x, int y, int z, int[] indicesOffset, BlockType[] neighbors) {
        this.blockType = blockType;

        boolean[] facesActive = new boolean[6];

        // calculate active faces
        for (int face = 0; face < 6; face++) {
            BlockType neighbor =neighbors[face];

            if (face != Side.TOP && blockType == BlockType.WATER) {
                facesActive[face] = false;
                continue;
            }

            if (neighbor == null || neighbor == BlockType.AIR) {
                facesActive[face] = true;
                activeFaces++;
                continue;
            }

            if (neighbor == blockType) {
                facesActive[face] = false;
                continue;
            }

            if (blockType.isTransparent()) {
                // for now don't render any vertices, underwater we just apply a filter
                facesActive[face] = false;
                continue;
            }
            
            if (neighbor.isTransparent()) {
                facesActive[face] = true;
                activeFaces++;
                continue;
            }

            facesActive[face] = false;
        }

        if (activeFaces == 0) {
            return;
        }

        // front
        int side = Side.FRONT;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                0 + x, 0 + y, 1 + z,
                1 + x, 0 + y, 1 + z,
                1 + x, 1 + y, 1 + z,
                0 + x, 1 + y, 1 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        // back
        side = Side.BACK;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                    1 + x, 0 + y, 0 + z,
                    0 + x, 0 + y, 0 + z,
                    0 + x, 1 + y, 0 + z,
                    1 + x, 1 + y, 0 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        // top
        side = Side.TOP;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                    0 + x, 1 + y, 1 + z,
                    1 + x, 1 + y, 1 + z,
                    1 + x, 1 + y, 0 + z,
                    0 + x, 1 + y, 0 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        // bottom
        side = Side.BOTTOM;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                    0 + x, 0 + y, 0 + z,
                    1 + x, 0 + y, 0 + z,
                    1 + x, 0 + y, 1 + z,
                    0 + x, 0 + y, 1 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        // right
        side = Side.RIGHT;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                    1 + x, 0 + y, 1 + z,
                    1 + x, 0 + y, 0 + z,
                    1 + x, 1 + y, 0 + z,
                    1 + x, 1 + y, 1 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        // left
        side = Side.LEFT;
        if (facesActive[side]) {
            grids[side].empty = false;
            grids[side].vertices = new float[]{
                    0 + x, 0 + y, 0 + z,
                    0 + x, 0 + y, 1 + z,
                    0 + x, 1 + y, 1 + z,
                    0 + x, 1 + y, 0 + z,
            };
            grids[side].indices = new int[]{
                    indicesOffset[side],
                1 + indicesOffset[side],
                3 + indicesOffset[side],
                3 + indicesOffset[side],
                1 + indicesOffset[side],
                2 + indicesOffset[side],
            };
        }

        for (side = 0; side < 6; side++) {
            if (facesActive[side]) {
                Vector2i texCoordsOffset = blockType.getTexCoordsOffset();
                /*
                grids[side].texCoords = new float[]{
                        0, 0,
                        1, 0,
                        1, 1,
                        0, 1,
                };
                */
                grids[side].texCoords = new float[]{
                        (0 + texCoordsOffset.x) * TEXTURE_DIV, (0 + texCoordsOffset.y) * TEXTURE_DIV,
                        (1 + texCoordsOffset.x) * TEXTURE_DIV, (0 + texCoordsOffset.y) * TEXTURE_DIV,
                        (1 + texCoordsOffset.x) * TEXTURE_DIV, (1 + texCoordsOffset.y) * TEXTURE_DIV,
                        (0 + texCoordsOffset.x) * TEXTURE_DIV, (1 + texCoordsOffset.y) * TEXTURE_DIV,
                };
            }
        }
    }

    public boolean isEmpty() {
        return activeFaces == 0;
        //return Arrays.stream(grids).allMatch((g) -> g.empty);
    }

    public boolean isEmpty(int side) {
        return grids[side].empty;
    }

    public int verticesCount(int side) {
        if (grids[side].empty) {
            return 0;
        }

        return grids[side].vertices.length;
    }

    public int indicesCount(int side) {
        if (grids[side].empty) {
            return 0;
        }

        return grids[side].indices.length;
    }

    public int texCoordsCount(int side) {
        if (grids[side].empty) {
            return 0;
        }

        return grids[side].texCoords.length;
    }

    public float[] getVertices(int side) {
        return grids[side].vertices;
    }

    public int[] getIndices(int side) {
        return grids[side].indices;
    }

    public float[] getTexCoords(int side) {
        return grids[side].texCoords;
    }

    public BlockType getBlockType() {
        return blockType;
    }
}
