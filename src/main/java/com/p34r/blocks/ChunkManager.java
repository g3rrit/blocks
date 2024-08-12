package com.p34r.blocks;

import org.joml.Vector3i;
import org.tinylog.Logger;

public class ChunkManager extends Thread {

    private Terrain terrain;
    private ChunkContainer chunkContainer;

    private volatile boolean stop = false;

    public ChunkManager() {
        this.chunkContainer = new ChunkContainer();
        this.terrain = new Terrain();
    }

    @Override
    public void run() {
        int x = -3;
        int y = -3;
        int z = -3;
        while (!stop) {
            Logger.info("Running chunk manager");

            int finalX = x;
            int finalY = x;
            int finalZ = x;
            chunkContainer.add(new Vector3i(x, y, z), () -> {
                return new Chunk(terrain, finalX, finalY, finalZ);
            });
            x++;
            y++;
            z++;

            // TODO: maybe adding needs to happen in main thread ?
            chunkContainer.addAll();
            //chunkContainer.removeAll();
            //chunkContainer.cleanupAll();

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: just expose forEach method
    public ChunkContainer getChunkContainer() {
        return chunkContainer;
    }

    public void setStop() {
        stop = true;
    }

}
