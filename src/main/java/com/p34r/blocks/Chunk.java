package com.p34r.blocks;

import org.joml.Matrix4f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class Chunk {
    public static final int CHUNK_SIZE = 16;

    private class MeshData {
        public float[] vertices;
        public int[] indices;
        public float[] texCoords;
        public int[] blockTypes;

        public MeshData(float[] vertices, int[] indices, float[] texCoords, int[] blockTypes) {
            this.vertices = vertices;
            this.indices = indices;
            this.texCoords = texCoords;
            this.blockTypes = blockTypes;
        }
    }
    private Map<BlockMaterial, Side<MeshData>> meshData;

    // position is in terms of the chunk size. I.e. 2, 0 -> 2 * 32, 0
    private Vector3i pos;
    private Map<BlockMaterial, Side<BlockMesh>> blockMeshes;
    private Matrix4f modelMatrix;
    private Terrain terrain;

    public Chunk(Terrain terrain, int x, int y, int z) {
        this.terrain = terrain;
        this.pos = new Vector3i(x, y, z);
        this.blockMeshes = new EnumMap<>(BlockMaterial.class);
        this.meshData = new EnumMap<>(BlockMaterial.class);

        this.modelMatrix = new Matrix4f();
        this.modelMatrix.translate(x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE);

        updateMesh(BlockMaterial.SOLID);
        updateMesh(BlockMaterial.WATER);
    }

    /**
     * This method must either be called by the constructor (in any thread)
     * or by the main thread
     */
    private void updateMesh(BlockMaterial blockMaterial) {
        if (!meshData.containsKey(blockMaterial)) {
            meshData.put(blockMaterial, new Side<>());
            blockMeshes.put(blockMaterial, new Side<>());
        }
        Side<MeshData> meshDataSides = meshData.get(blockMaterial);
        Side<BlockMesh> blockMeshSides = blockMeshes.get(blockMaterial);

        ArrayList<BlockGrid> blockGrids = new ArrayList<>();

        int[] indicesOffset = new int[6];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++){
                    BlockType block = getBlock(x, y, z);
                    if (block.getBlockMaterial() != blockMaterial) {
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

            if (blockMaterial == BlockMaterial.WATER && side != Side.TOP) {
                continue;
            }

            int verticesCount = 0;
            int indicesCount = 0;
            int texCoordsCount = 0;

            for (BlockGrid blockGrid : blockGrids) {
                verticesCount += blockGrid.verticesCount(side);
                indicesCount += blockGrid.indicesCount(side);
                texCoordsCount += blockGrid.texCoordsCount(side);
            }

            if (verticesCount == 0) {
                continue;
            }

            float[] vertices = new float[verticesCount];
            int[] indices = new int[indicesCount];
            float[] texCoords = new float[texCoordsCount];
            int[] blockTypes = new int[verticesCount / 3];

            {
                int iV = 0;
                int iI = 0;
                int iT = 0;
                int iB = 0;
                for (BlockGrid blockGrid : blockGrids) {
                    if (blockGrid.isEmpty(side)) {
                        continue;
                    }

                    float[] verticesB = blockGrid.getVertices(side);
                    int[] indicesB = blockGrid.getIndices(side);
                    float[] texCoordsB = blockGrid.getTexCoords(side);

                    for (float v : verticesB) {
                        vertices[iV++] = v;
                    }
                    for (int i : indicesB) {
                        indices[iI++] = i;
                    }
                    for (float t : texCoordsB) {
                        texCoords[iT++] = t;
                    }

                    for (int i = 0; i < 4; i++) {
                        blockTypes[iB++] = blockGrid.getBlockType().getIdx();
                    }
                }
            }

            // This cleanup must only happen in the main thread
            BlockMesh blockMesh = blockMeshSides.get(side);
            if (blockMesh != null) {
                blockMesh.cleanup();
                blockMeshSides.set(side, null);
            }

            meshDataSides.set(side, new MeshData(vertices, indices, texCoords, blockTypes));
        }
    }

    public void cleanup() {
        for (Side<BlockMesh> blockMeshSides: blockMeshes.values()) {
            for (int i = 0; i < 6; i++) {
                BlockMesh blockMesh = blockMeshSides.get(i);
                if (blockMesh != null) {
                    blockMesh.cleanup();
                }
            }
        }
    }

    public boolean isSideEmpty(BlockMaterial blockMaterial, int side) {
        Side<MeshData> meshDataSides = meshData.get(blockMaterial);
        if (meshDataSides == null) {
            return true;
        }
        return meshDataSides.get(side) == null;
    }

    public boolean isEmpty() {
        for (int i = 0; i < 6; i++) {
            for (BlockMaterial blockMaterial: BlockMaterial.values()) {
                if (!isSideEmpty(blockMaterial, i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public BlockMesh getMesh(BlockMaterial blockMaterial, int side) {
        if (!meshData.containsKey(blockMaterial)) {
            return null;
        }

        MeshData mesh = meshData.get(blockMaterial).get(side);

        if (mesh == null) {
            return null;
        }

        Side<BlockMesh> blockMeshSides = blockMeshes.get(blockMaterial);

        // lazy initialization, as this must happen in the main thread
        if (blockMeshSides.get(side) == null) {
            blockMeshSides.set(side, new BlockMesh(mesh.vertices, mesh.indices, mesh.texCoords, mesh.blockTypes));
        }

        return blockMeshSides.get(side);
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
