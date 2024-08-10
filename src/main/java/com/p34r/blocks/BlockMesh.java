package com.p34r.blocks;

public class BlockMesh {
    /**
     * Mesh:
     *      V3D *--------* V2C
     *         /|       /|
     *        / |V0A   / |
     *   V7B *--*-----*  * V1B
     *       | /   V6A| /
     *       |/       |/
     *   V4C *--------* V5D
     *
     * Texture:
     *     D *--------* C
     *       |        |
     *       |        |
     *     A *--------* B
     */
    private static float[] positionsNorm = new float[]{
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
    private static float[] textCoordsNorm = new float[]{
            // A
            0, 1,
            // B
            1, 1,
            // C
            1, 0,
            // D
            0, 0,
            // C
            1, 0,
            // D
            0, 0,
            // A
            0, 1,
            // B
            1, 1,
    };
    private static int[] indicesNorm = new int[]{
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

    private float[] positions;
    private float[] textCoords;
    private int[] indices;

    private boolean empty;

    /**
     * Generates the temporary mesh of the current block
     *
     * Faces:
     * 0 - front
     * 1 - back
     * 2 - top
     * 3 - bottom
     * 4 - left
     * 5 - right
     */
    public BlockMesh(int x, int y, int z, int textOffsetX, int textureOffsetY, int indicesOffset, BlockType[] neighbors) {
        this.empty = false;

        boolean[] facesActive = new boolean[6];
        boolean[] verticesActive = new boolean[8];

        int activeFacesCount = 0;

        // calculate active faces
        for (int face = 0; face < 6; face++) {
            if (neighbors[face] == null) {
                facesActive[face] = true;
                activeFacesCount++;
                continue;
            }
            switch (neighbors[face]) {
                case BlockType.AIR -> {
                    facesActive[face] = true;
                    activeFacesCount++;
                }
                default -> facesActive[face] = false;
            }
        }

        if (activeFacesCount == 0) {
            this.empty = true;
            return;
        }

        // calculate active vertices
        verticesActive[0] = facesActive[4] || facesActive[1] || facesActive[3];
        verticesActive[1] = facesActive[3] || facesActive[1] || facesActive[5];
        verticesActive[2] = facesActive[5] || facesActive[1] || facesActive[2];
        verticesActive[3] = facesActive[2] || facesActive[1] || facesActive[4];
        verticesActive[4] = facesActive[0] || facesActive[3] || facesActive[4];
        verticesActive[5] = facesActive[5] || facesActive[3] || facesActive[0];
        verticesActive[6] = facesActive[2] || facesActive[5] || facesActive[0];
        verticesActive[7] = facesActive[2] || facesActive[0] || facesActive[4];

        int activeVerticesCount = 0;
        int[] vertexMissingLT = new int[8];
        for (int vertex = 0; vertex < 8; vertex++) {
            vertexMissingLT[vertex] = vertex - activeVerticesCount;
            if (verticesActive[vertex]) {
                activeVerticesCount++;
            }
        }

        this.positions = new float[3 * activeVerticesCount];
        this.textCoords = new float[2 * activeVerticesCount];
        this.indices = new int[6 * activeFacesCount];

        {
            int i = 0;
            for (int vertex = 0; vertex < 8; vertex++) {
                if (!verticesActive[vertex]) {
                    continue;
                }

                this.positions[3 * i] = positionsNorm[3 * vertex] + x;
                this.positions[3 * i + 1] = positionsNorm[3 * vertex + 1] + y;
                this.positions[3 * i + 2] = positionsNorm[3 * vertex + 2] + z;

                // TODO: add block texture
                this.textCoords[2 * i] = textCoordsNorm[2 * vertex] + textOffsetX;
                this.textCoords[2 * i + 1] = textCoordsNorm[2 * vertex + 1] + textureOffsetY;

                i++;
            }
        }

        {
            int i = 0;
            for (int face = 0; face < 6; face++) {
                if (!facesActive[face]) {
                    continue;
                }

                this.indices[6 * i] = indicesNorm[6 * face] - vertexMissingLT[indicesNorm[6 * face]] + indicesOffset;
                this.indices[6 * i + 1] = indicesNorm[6 * face + 1] - vertexMissingLT[indicesNorm[6 * face + 1]] + indicesOffset;
                this.indices[6 * i + 2] = indicesNorm[6 * face + 2] - vertexMissingLT[indicesNorm[6 * face + 2]] + indicesOffset;
                this.indices[6 * i + 3] = indicesNorm[6 * face + 3] - vertexMissingLT[indicesNorm[6 * face + 3]] + indicesOffset;
                this.indices[6 * i + 4] = indicesNorm[6 * face + 4] - vertexMissingLT[indicesNorm[6 * face + 4]] + indicesOffset;
                this.indices[6 * i + 5] = indicesNorm[6 * face + 5] - vertexMissingLT[indicesNorm[6 * face + 5]] + indicesOffset;

                i++;
            }
        }
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public int verticesCount() {
        return this.positions.length;
    }

    public int textCoordsCount() {
        return this.textCoords.length;
    }

    public int indicesCount() {
        return this.indices.length;
    }

    public float[] getVertices() {
        return this.positions;
    }

    public float[] getTextCoords() {
        return this.textCoords;
    }

    public int[] getIndices() {
        return this.indices;
    }
}
