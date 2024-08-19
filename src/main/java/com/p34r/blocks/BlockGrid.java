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
        public float[] textCoords = null;
    }

    /**
     * Mesh:
     *      V3  *--------* V2
     *         /|       /|
     *        / |V0    / |
     *   V7  *--*-----*  * V1
     *       | /   V6 | /
     *       |/       |/
     *   V4  *--------* V5
     */
    private static final float[] verticesNorm = new float[]{
            // V0
            0, 0, 0,
            // V1
            1, 0, 0,
            // V2
            1, 1, 0,
            // V3
            0, 1, 0,
            // V4
            0, 0, 1,
            // V5
            1, 0, 1,
            // V6
            1, 1, 1,
            // V7
            0, 1, 1,
    };

    private static final int[] indicesNorm = new int[]{
            // Front face
            7, 4, 5, 5, 6, 7,
            // Back face
            2, 1, 0, 0, 3, 2,
            // Top face
            3, 7, 6, 6, 2, 3,
            // Bottom face
            4, 0, 1, 1, 5, 4,
            // Left face
            3, 0, 4, 4, 7, 3,
            // Right face
            6, 5, 1, 1, 2, 6,
    };

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
            if (neighbors[face] == null || neighbors[face] == BlockType.AIR) {
                facesActive[face] = true;
                activeFaces++;
            } else {
                facesActive[face] = false;
            }
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
                Vector2i textCoordsOffset = blockType.getTextCoordsOffset();
                /*
                grids[side].textCoords = new float[]{
                        0, 1,
                        1, 1,
                        0, 1,
                        0, 0,
                };
                */
                grids[side].textCoords = new float[]{
                        (0 + textCoordsOffset.x) * TEXTURE_DIV, (1 + textCoordsOffset.y) * TEXTURE_DIV,
                        (1 + textCoordsOffset.x) * TEXTURE_DIV, (1 + textCoordsOffset.y) * TEXTURE_DIV,
                        (0 + textCoordsOffset.x) * TEXTURE_DIV, (1 + textCoordsOffset.y) * TEXTURE_DIV,
                        (0 + textCoordsOffset.x) * TEXTURE_DIV, (0 + textCoordsOffset.y) * TEXTURE_DIV,
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

    public int textCoordsCount(int side) {
        if (grids[side].empty) {
            return 0;
        }

        return grids[side].textCoords.length;
    }

    public float[] getVertices(int side) {
        return grids[side].vertices;
    }

    public int[] getIndices(int side) {
        return grids[side].indices;
    }

    public float[] getTextCoords(int side) {
        return grids[side].textCoords;
    }

    public BlockType getBlockType() {
        return blockType;
    }
}
