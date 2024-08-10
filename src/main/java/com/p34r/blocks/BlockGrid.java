package com.p34r.blocks;

import java.util.Arrays;
import java.util.Objects;

public class BlockGrid {
    public class Grid {
        public boolean empty = true;
        public float[] vertices = null;
        public int[] indices = null;
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
        //this.grids = new Grid[6];

        boolean[] facesActive = new boolean[6];
        boolean[] verticesActive = new boolean[8];

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
        if (facesActive[Side.FRONT]) {
            grids[Side.FRONT].empty = false;
            grids[Side.FRONT].vertices = new float[]{
                0 + x, 0 + y, 1 + z,
                1 + x, 0 + y, 1 + z,
                1 + x, 1 + y, 1 + z,
                0 + x, 1 + y, 1 + z,
            };
            grids[Side.FRONT].indices = new int[]{
                    indicesOffset[Side.FRONT],
                1 + indicesOffset[Side.FRONT],
                3 + indicesOffset[Side.FRONT],
                3 + indicesOffset[Side.FRONT],
                1 + indicesOffset[Side.FRONT],
                2 + indicesOffset[Side.FRONT],
            };
        }

        // back
        if (facesActive[Side.BACK]) {
            grids[Side.BACK].empty = false;
            grids[Side.BACK].vertices = new float[]{
                    1 + x, 0 + y, 0 + z,
                    0 + x, 0 + y, 0 + z,
                    0 + x, 1 + y, 0 + z,
                    1 + x, 1 + y, 0 + z,
            };
            grids[Side.BACK].indices = new int[]{
                    indicesOffset[Side.BACK],
                1 + indicesOffset[Side.BACK],
                3 + indicesOffset[Side.BACK],
                3 + indicesOffset[Side.BACK],
                1 + indicesOffset[Side.BACK],
                2 + indicesOffset[Side.BACK],
            };
        }

        // top
        if (facesActive[Side.TOP]) {
            grids[Side.TOP].empty = false;
            grids[Side.TOP].vertices = new float[]{
                    0 + x, 1 + y, 1 + z,
                    1 + x, 1 + y, 1 + z,
                    1 + x, 1 + y, 0 + z,
                    0 + x, 1 + y, 0 + z,
            };
            grids[Side.TOP].indices = new int[]{
                    indicesOffset[Side.TOP],
                1 + indicesOffset[Side.TOP],
                3 + indicesOffset[Side.TOP],
                3 + indicesOffset[Side.TOP],
                1 + indicesOffset[Side.TOP],
                2 + indicesOffset[Side.TOP],
            };
        }

        // bottom
        if (facesActive[Side.BOTTOM]) {
            grids[Side.BOTTOM].empty = false;
            grids[Side.BOTTOM].vertices = new float[]{
                    0 + x, 0 + y, 0 + z,
                    1 + x, 0 + y, 0 + z,
                    1 + x, 0 + y, 1 + z,
                    0 + x, 0 + y, 1 + z,
            };
            grids[Side.BOTTOM].indices = new int[]{
                    indicesOffset[Side.BOTTOM],
                1 + indicesOffset[Side.BOTTOM],
                3 + indicesOffset[Side.BOTTOM],
                3 + indicesOffset[Side.BOTTOM],
                1 + indicesOffset[Side.BOTTOM],
                2 + indicesOffset[Side.BOTTOM],
            };
        }

        // right
        if (facesActive[Side.RIGHT]) {
            grids[Side.RIGHT].empty = false;
            grids[Side.RIGHT].vertices = new float[]{
                    1 + x, 0 + y, 1 + z,
                    1 + x, 0 + y, 0 + z,
                    1 + x, 1 + y, 0 + z,
                    1 + x, 1 + y, 1 + z,
            };
            grids[Side.RIGHT].indices = new int[]{
                    indicesOffset[Side.RIGHT],
                1 + indicesOffset[Side.RIGHT],
                3 + indicesOffset[Side.RIGHT],
                3 + indicesOffset[Side.RIGHT],
                1 + indicesOffset[Side.RIGHT],
                2 + indicesOffset[Side.RIGHT],
            };
        }

        // left
        if (facesActive[Side.LEFT]) {
            grids[Side.LEFT].empty = false;
            grids[Side.LEFT].vertices = new float[]{
                    0 + x, 0 + y, 0 + z,
                    0 + x, 0 + y, 1 + z,
                    0 + x, 1 + y, 1 + z,
                    0 + x, 1 + y, 0 + z,
            };
            grids[Side.LEFT].indices = new int[]{
                    indicesOffset[Side.LEFT],
                1 + indicesOffset[Side.LEFT],
                3 + indicesOffset[Side.LEFT],
                3 + indicesOffset[Side.LEFT],
                1 + indicesOffset[Side.LEFT],
                2 + indicesOffset[Side.LEFT],
            };
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

    public float[] getVertices(int side) {
        return grids[side].vertices;
    }

    public int[] getIndices(int side) {
        return grids[side].indices;
    }

    public BlockType getBlockType() {
        return blockType;
    }
}
