package com.p34r.blocks;

import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Chunk {
    public static final int CHUNK_SIZE = 8;

    private class MeshData {
        public float[] vertices;
        public int[] indices;
        public float[] textCoords;

        public MeshData(float[] vertices, int[] indices, float[] textCoords) {
            this.vertices = vertices;
            this.indices = indices;
            this.textCoords = textCoords;
        }
    }
    private MeshData[] meshData;

    // position is in terms of the chunk size. I.e. 2, 0 -> 2 * 32, 0
    private Vector3i pos;
    private BlockMesh[] blockMeshes;
    private Matrix4f modelMatrix;
    private Terrain terrain;

    public Chunk(Terrain terrain, int x, int y, int z) {
        this.terrain = terrain;
        this.pos = new Vector3i(x, y, z);
        this.blockMeshes = new BlockMesh[6];
        this.meshData = new MeshData[6];

        this.modelMatrix = new Matrix4f();
        this.modelMatrix.translate(x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE);

        updateMesh();
    }

    private void updateMesh() {
        ArrayList<BlockGrid> blockGrids = new ArrayList<>();

        int[] indicesOffset = new int[6];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++){
                    BlockType block = getBlock(x, y, z);
                    if (block == BlockType.AIR) {
                        continue;
                    }

                    /* old approach, it should be as fast querying the blocks from the terrain
                    BlockType[] blockNeighbors = new BlockType[] {
                            (z + 1 >= CHUNK_SIZE) ? null : blocks[x][y][z + 1],
                            (z - 1 < 0) ? null : blocks[x][y][z - 1],
                            (y + 1 >= CHUNK_SIZE) ? null : blocks[x][y + 1][z],
                            (y - 1 < 0) ? null : blocks[x][y - 1][z],
                            (x + 1 >= CHUNK_SIZE) ? null : blocks[x + 1][y][z],
                            (x - 1 < 0) ? null : blocks[x - 1][y][z],
                    };
                    */
                    BlockType[] blockNeighbors = new BlockType[]{
                        getBlock(x, y, z + 1),
                        getBlock(x, y, z - 1),
                        getBlock(x, y + 1, z),
                        getBlock(x, y - 1, z),
                        getBlock(x + 1, y, z),
                        getBlock(x - 1, y, z),
                    };

                    BlockGrid blockGrid = new BlockGrid(block, x, y, z, indicesOffset, blockNeighbors);

                    if (blockGrid.isEmpty()) {
                        continue;
                    }

                    for (int i = 0; i < 6; i++) {
                        // TODO: there will be always 4 vertices (* 3)
                        indicesOffset[i] += blockGrid.verticesCount(i) / 3;
                    }

                    blockGrids.add(blockGrid);
                }
            }
        }

        for (int side = 0; side < 6; side++) {

            int verticesCount = 0;
            int indicesCount = 0;
            int textCoordsCount = 0;

            for (BlockGrid blockGrid : blockGrids) {
                verticesCount += blockGrid.verticesCount(side);
                indicesCount += blockGrid.indicesCount(side);
                textCoordsCount += blockGrid.textCoordsCount(side);
            }

            if (verticesCount == 0) {
                continue;
            }

            float[] vertices = new float[verticesCount];
            int[] indices = new int[indicesCount];
            float[] textCoords = new float[textCoordsCount];

            {
                int iV = 0;
                int iI = 0;
                int iT = 0;
                for (BlockGrid blockGrid : blockGrids) {
                    if (blockGrid.isEmpty(side)) {
                        continue;
                    }

                    float[] verticesB = blockGrid.getVertices(side);
                    int[] indicesB = blockGrid.getIndices(side);
                    float[] textCoordsB = blockGrid.getTextCoords(side);

                    for (float v : verticesB) {
                        vertices[iV++] = v;
                    }
                    for (int i : indicesB) {
                        indices[iI++] = i;
                    }
                    for (float t : textCoordsB) {
                        textCoords[iT++] = t;
                    }
                }
            }

            this.meshData[side] = new MeshData(vertices, indices, textCoords);
        }
    }

    public void cleanup() {
        for (BlockMesh blockMesh : blockMeshes) {
            if (blockMesh != null) {
                blockMesh.cleanup();
            }
        }
    }

    public boolean isSideEmpty(int side) {
        return meshData[side] == null;
    }

    public BlockMesh getMesh(int side) {
        if (meshData[side] == null) {
            return null;
        }

        // lazy initialization, as this must happen in the main thread
        if (blockMeshes[side] == null) {
            blockMeshes[side] = new BlockMesh(meshData[side].vertices, meshData[side].indices, meshData[side].textCoords);
        }

        return blockMeshes[side];
    }

    public BlockType getBlock(int x, int y, int z) {
        return terrain.get(pos.x * CHUNK_SIZE + x, pos.y * CHUNK_SIZE + y, pos.z * CHUNK_SIZE + z);
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public Vector3i getPos() {
        return pos;
    }
}
