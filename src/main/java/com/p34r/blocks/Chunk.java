package com.p34r.blocks;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;

public class Chunk {
    public static final int CHUNK_SIZE = 8;

    // position is in terms of the chunk size. I.e. 2, 0 -> 2 * 32, 0
    private Vector2i pos;

    private BlockType[][][] blocks;

    /**
     * 0 - front
     * 1 - back
     * 2 - top
     * 3 - bottom
     * 4 - left
     * 5 - right
     */
    private Chunk[] neighbors;

    private Mesh mesh;

    public Chunk(int x, int y, int z) {
        this.pos = new Vector2i(x, y);
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                for (int n = 0; n < CHUNK_SIZE; n++) {
                    this.blocks[i][j][n] = BlockType.DIRT;
                }
            }
        }
        this.neighbors = new Chunk[6];

        updateMesh();
    }

    private void updateMesh() {
        ArrayList<BlockMesh> blockMeshes = new ArrayList<>();

        int verticesCount = 0;
        int textCoordsCount = 0;
        int indicesCount = 0;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++){
                    if (blocks[x][y][z] == BlockType.AIR) {
                        continue;
                    }
                    // TODO: check for neighbors in other chunks
                    BlockType[] blockNeighbors = new BlockType[] {
                            (z + 1 >= CHUNK_SIZE) ? null : blocks[x][y][z + 1],
                            (z - 1 < 0) ? null : blocks[x][y][z - 1],
                            (y + 1 >= CHUNK_SIZE) ? null : blocks[x][y + 1][z],
                            (y - 1 < 0) ? null : blocks[x][y - 1][z],
                            (x - 1 < 0) ? null : blocks[x - 1][y][z],
                            (x + 1 >= CHUNK_SIZE) ? null : blocks[x + 1][y][z],
                    };

                    BlockMesh blockMesh = new BlockMesh(x, y, z, 0, 0, verticesCount / 3, blockNeighbors);

                    if (blockMesh.isEmpty()) {
                        continue;
                    }

                    verticesCount += blockMesh.verticesCount();
                    textCoordsCount += blockMesh.textCoordsCount();
                    indicesCount += blockMesh.indicesCount();

                    blockMeshes.add(blockMesh);
                }
            }
        }

        float[] vertices = new float[verticesCount];
        float[] textCoords = new float[textCoordsCount];
        int[] indices = new int[indicesCount];

        {
            int iV = 0;
            int iT = 0;
            int iI = 0;
            for (BlockMesh blockMesh : blockMeshes) {
                float[] verticesB = blockMesh.getVertices();
                float[] textCoordsB = blockMesh.getTextCoords();
                int[] indicesB = blockMesh.getIndices();

                for (float v : verticesB) {
                    vertices[iV++] = v;
                }
                for (float t : textCoordsB) {
                    textCoords[iT++] = t;
                }
                for (int i: indicesB) {
                    indices[iI++] = i;
                }
            }
        }

        this.mesh = new Mesh(vertices, textCoords, indices);
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    public BlockType getBlock(int x, int y, int z) {
        return this.blocks[x][y][z];
    }
}
