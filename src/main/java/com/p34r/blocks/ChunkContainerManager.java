package com.p34r.blocks;

import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.tinylog.Logger;

public class ChunkContainerManager {

    /** This class has an internal lock */
    private ChunkContainer chunkContainer;

    private Lock positionLock;
    private ArrayList<Vector3i> positions;

    private Lock removeLock;
    private ArrayList<Vector3i> removeList;

    private Lock addLock;
    private HashMap<Vector3i, Chunk> addList;

    private Lock cleanupLock;
    private ArrayList<Chunk> cleanupList;

    private volatile int chunkCount = 0;

    public ChunkContainerManager() {
        this.chunkContainer = new ChunkContainer();

        this.positions = new ArrayList<>();
        this.removeList = new ArrayList<>();
        this.addList = new HashMap<>();
        this.cleanupList = new ArrayList<>();

        this.positionLock = new ReentrantLock();
        this.removeLock = new ReentrantLock();
        this.addLock = new ReentrantLock();
        this.cleanupLock = new ReentrantLock();
    }

    public void addAll() {
        positionLock.lock();
        addLock.lock();
        chunkContainer.lock();
        try {
            if (addList.isEmpty()) {
                return;
            }
            chunkContainer.addAll(addList.values().stream().filter((c) -> !c.isEmpty()).toList());
            positions.addAll(addList.keySet());
            addList.clear();
            chunkCount = positions.size();
            Logger.info("[A] Chunk count: " + chunkContainer.size() + " - " + positions.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            positionLock.unlock();
            addLock.unlock();
            chunkContainer.unlock();
        }
    }

    public void removeAll() {
        positionLock.lock();
        removeLock.lock();
        cleanupLock.lock();
        chunkContainer.lock();
        try {
            if (removeList.isEmpty()) {
                return;
            }

            cleanupList.addAll(chunkContainer.removeAll(removeList));
            positions.removeAll(removeList);
            removeList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            positionLock.unlock();
            removeLock.unlock();
            cleanupLock.unlock();
            chunkContainer.unlock();
        }
    }

    /** Must be called from main thread */
    public void cleanupAll() {
        cleanupLock.lock();
        try {
            if (cleanupList.isEmpty()) {
                return;
            }

            for (Chunk chunk : cleanupList) {
                chunk.cleanup();
            }
            cleanupList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanupLock.unlock();
        }
    }

    public void forEachPosition(Consumer<Vector3i> consumer) {
        positionLock.lock();
        try {
            for (Vector3i position : positions) {
                consumer.accept(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            positionLock.unlock();
        }
    }

    public void add(Vector3i pos, Supplier<Chunk> supplier) {
        addLock.lock();
        positionLock.lock();
        try {
            if (addList.containsKey(pos) || positions.contains(pos)) {
                return;
            }

            addList.put(pos, supplier.get());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            addLock.unlock();
            positionLock.unlock();
        }
    }

    public void remove(Vector3i pos) {
        removeLock.lock();
        try {
            if (removeList.contains(pos)) {
                return;
            }
            removeList.add(pos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            removeLock.unlock();
        }
    }

    /** Must be called from main thread */
    public void cleanup() {
        positionLock.lock();
        removeLock.lock();
        cleanupLock.lock();
        addLock.lock();
        try {
            chunkContainer.cleanup();
            addList.values().forEach(Chunk::cleanup);
            cleanupList.forEach(Chunk::cleanup);

            addList.clear();
            cleanupList.clear();
            removeList.clear();
            positions.clear();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            positionLock.unlock();
            removeLock.unlock();
            cleanupLock.unlock();
            addLock.unlock();
        }
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public ChunkContainer getChunkContainer() {
        chunkContainer.lock();
        return chunkContainer;
    }
}

