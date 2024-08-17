package com.p34r.blocks;

import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Consumer;

public class ChunkManager extends Thread {

    private static final long UPDATES_PER_SECONDS = 10;
    private static final double ADD_TIMEOUT = 0.5;
    private static final double REMOVE_TIMEOUT = 0.3;
    private static final double ADD_CHUNK_TIMEOUT = 0.1;
    private static final double REMOVE_CHUNK_TIMEOUT = 0.3;

    //private static final double CHUNK_REMOVE_DISTANCE = 10;
    private static final int CHUNK_ADD_COUNT = 400;
    private static final int MAX_CHUNK_COUNT = 4000;
    private static final int MAX_CHUNK_COUNT_BUFFER = 1000;

    private Terrain terrain;
    private ChunkContainer chunkContainer;

    private volatile boolean running = true;

    private Scene scene;

    public ChunkManager(Scene scene) {
        this.chunkContainer = new ChunkContainer();
        this.terrain = new Terrain();
        this.scene = scene;
    }

    @Override
    public void run() {
        long startTime = 0;
        long dt = 0;
        double dtf = 0;

        double addTimeout = ADD_TIMEOUT;
        double removeTimeout = REMOVE_TIMEOUT;
        double addChunkTimeout = ADD_CHUNK_TIMEOUT;
        double removeChunkTimeout = REMOVE_CHUNK_TIMEOUT;

        while (running) {
            dtf = (double) dt / Defs.NANOS_IN_SECONDS;

            startTime = System.nanoTime();

            if ((addTimeout -= dtf) <= 0) {
                chunkContainer.addAll();
                addTimeout = ADD_TIMEOUT;
            }

            if ((removeTimeout -= dtf) <= 0) {
                chunkContainer.removeAll();
                removeTimeout = REMOVE_TIMEOUT;
            }

            if ((addChunkTimeout -= dtf) <= 0) {
                addChunk();
                addChunkTimeout = ADD_CHUNK_TIMEOUT;
            }

            if ((removeChunkTimeout -= dtf) <= 0) {
                removeChunk();
                removeChunkTimeout = REMOVE_CHUNK_TIMEOUT;
            }

            dt = 0;
            while ((dt = (System.nanoTime() - startTime)) < (Defs.NANOS_IN_SECONDS / UPDATES_PER_SECONDS)) {
                try {
                    Thread.sleep(1);  // TODO: find better value here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void forEach(Consumer<Chunk> consumer) {
        if (!running) {
            return;
        }

        chunkContainer.forEach(consumer);
    }

    public void gc() {
        if (!running) {
            return;
        }

        chunkContainer.cleanupAll();
    }

    public void exit() {
        running = false;
    }

    public void cleanup() {
        if (running) {
            throw new RuntimeException("Can't cleanup ChunkManager while running");
        }
        chunkContainer.cleanup();
    }

    private void removeChunk() {
        int chunkCount = chunkContainer.getChunkCount();
        if (chunkCount <= MAX_CHUNK_COUNT + MAX_CHUNK_COUNT_BUFFER) {
            return;
        }

        Vector3f pos_ = scene.getCamera().getPositionC();
        // The distance is too inaccurate if we take y into account. If the terrain is flat, this might be ok
        Vector3i pos = new Vector3i((int) pos_.x / Chunk.CHUNK_SIZE, 0, (int) pos_.z / Chunk.CHUNK_SIZE);

        ArrayList<Vector3i> removeList = new ArrayList<>();

        // TODO: this can be optimized
        chunkContainer.forEachPosition((chunkPos) -> {
            if (removeList.size() < MAX_CHUNK_COUNT_BUFFER / 2) {
                removeList.add(chunkPos);
                removeList.sort(Comparator.comparingDouble(pos::distance));
                return;
            }

            for (int i = 0; i < removeList.size(); i++) {
                if (pos.distance(removeList.get(i)) < pos.distance(chunkPos)) {
                    removeList.set(i, chunkPos);
                    removeList.sort(Comparator.comparingDouble(pos::distance));
                    return;
                }
            }
        });

        for (Vector3i chunkPos: removeList) {
            Logger.info("Removing chunk: " + chunkPos);
            chunkContainer.remove(chunkPos);
        }
    }

    private void addChunk() {
        Vector3f pos_ = scene.getCamera().getPositionC();
        Vector3i pos = new Vector3i((int) pos_.x / Chunk.CHUNK_SIZE, (int) pos_.y / Chunk.CHUNK_SIZE, (int) pos_.z / Chunk.CHUNK_SIZE);

        ArrayList<Vector3i> allPositions = new ArrayList<>();
        int MIN_Y = -3;
        int MAX_Y = 3;
        int x = 0;
        int y = -MAX_Y;
        int z = 0;
        int layer = 1;
        int leg = 0;
        for (int i = 0; i < MAX_CHUNK_COUNT; i++) {
            allPositions.add(new Vector3i(x + pos.x, y, z + pos.z));

            if (y < MAX_Y) {
                y++;
                continue;
            }

            if (leg == 0) {
                x += 1;
                if (x == layer) {
                    leg += 1;
                }
            } else if (leg == 1) {
                z += 1;
                if (z == layer) {
                    leg += 1;
                }
            } else if (leg == 2) {
                x -= 1;
                if (-x == layer) {
                    leg += 1;
                }
            } else if (leg == 3) {
                z -= 1;
                if (-z == layer) {
                    leg = 0;
                    layer += 1;
                }
            }
            y = MIN_Y;
        }

        chunkContainer.forEachPosition(allPositions::remove);

        int add_count = 0;
        for (Vector3i aPos: allPositions) {
            //Logger.info("Adding chunk: " + aPos);

            chunkContainer.add(aPos, () -> {
                return new Chunk(terrain, aPos.x, aPos.y, aPos.z);
            });
            add_count++;
            if (add_count >= CHUNK_ADD_COUNT) {
                return;
            }
        }
    }
}
