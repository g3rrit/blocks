package com.p34r.blocks;

import org.joml.Vector3i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChunkContainer implements AutoCloseable {

    private Lock _lock;

    private ArrayList<Chunk> chunks;

    public ChunkContainer() {
        this._lock = new ReentrantLock();
        this.chunks = new ArrayList<>();
    }

    public void add(Chunk chunk) {
        chunks.add(chunk);
    }

    public void addAll(List<Chunk> addChunks) {
        chunks.addAll(addChunks);
    }

    public List<Chunk> getAll() {
        return chunks;
    }

    public Chunk remove(Vector3i pos) {
        for (int i = 0; i < chunks.size(); i++) {
            if (chunks.get(i).getPos() == pos) {
                return chunks.remove(i);
            }
        }
        return null;
    }

    public List<Chunk> removeAll(List<Vector3i> pos) {
        ArrayList<Chunk> res = new ArrayList<>();
        int removed = 0;
        int size = chunks.size();
        for (int i = 0; i < size - removed;) {
            Chunk c = chunks.get(i);
            if (pos.stream().anyMatch((p) -> c.getPos() == p)) {
                // TODO: we dont reach this...
                Logger.info("Removing chunk!!!!!!!!");
                res.add(chunks.remove(i));
                removed++;
                continue;
            }
            i++;
        }
        return res;
    }

    public void cleanup() {
        for (Chunk chunk: chunks) {
            chunk.cleanup();
        }
        chunks.clear();
    }

    public int size() {
        return chunks.size();
    }

    /**
     * Must only be called from the ChunkContainerManager
     */
    public void lock() {
        _lock.lock();
    }

    public void unlock() {
        _lock.unlock();
    }

    @Override
    public void close() throws Exception {
        unlock();
    }
}
