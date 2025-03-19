package com.p34r.blocks;

import org.tinylog.Logger;

public class Terrain {

    public final int WATER_HEIGHT = 0;
    public final int TREE_HEIGHT = 5;

    private int seed = 42;

    private double getOctave(double length, double scale, int x, int z) {
        return SimplexNoise.noise((double)x / length, (double)z / length) * scale;
    }

    private double getDensity3d(double length, int x, int y, int z) {
        return SimplexNoise.noise((double)x / length, (double)y / length, (double)z / length);
    }

    private int posHash(int x, int z) {
        int hash = x;
        hash = ((x >> 16) ^ x) * 0x45d9f3b;
        hash = ((hash >> 16) ^ hash) * 0x45d9f3d;
        hash = ((hash >> 16) ^ hash);
        hash = hash ^ z;
        hash = ((hash >> 16) ^ hash) * 0x45d9f3b;
        hash = ((hash >> 16) ^ hash) * 0x45d9f3d;
        hash = ((hash >> 16) ^ hash);

        return hash;
    }

    private boolean isTree(int x, int z) {
        int ph = posHash(x, z);
        return ph % 40 == 0;
    }

    private boolean isLeaf(int x, int z) {
        if (isTree(x + 1, z)) { return true; }
        if (isTree(x + 1, z + 1)) { return true; }
        if (isTree(x, z + 1)) { return true; }
        if (isTree(x - 1, z + 1)) { return true; }
        if (isTree(x - 1, z)) { return true; }
        if (isTree(x - 1, z - 1)) { return true; }
        if (isTree(x, z - 1)) { return true; }
        if (isTree(x + 1, z - 1)) { return true; }

        return false;
    }

    public BlockType get(int x, int y, int z) {
        if (y < -20) {
            return BlockType.DIRT;
        }

        int octaves = 20;
        double height = 0;
        double lacunarity = 30;
        double persistance = 20;
        //for (int i = 0; i <= octaves; i++) {
        height += getOctave(300, 40, x, z);
        height += getOctave(30, 5, x, z);
        height += getOctave(20, 1, x, z);
        //}

        if (y < height) {
            return BlockType.GRASS;
        }

        if (y <= WATER_HEIGHT) {
            return BlockType.WATER;
        }

        if (height >= WATER_HEIGHT && y - height < TREE_HEIGHT) {
            if (isTree(x, z)) {
                return BlockType.WOOD;
            }
        }
        if (height >= WATER_HEIGHT && y - height < TREE_HEIGHT + 1 && y - height >= 3) {
            if (isLeaf(x, z)) {
                return BlockType.LEAF;
            }
        }

        return BlockType.AIR;

        /*
        double height = 0;
        int octaves = 2;
        double lacunarity = 100;
        double persistance = 20;
        for (int o = 0; o <= octaves; o++) {
            height += getOctave(lacunarity * Math.pow(4, o), persistance * 1/Math.pow(4, o), x, z);
        }

        double density = 0;

        for (int o = 0; o < 2; o++) {
            density += getDensity(100 * (1 / Math.pow(2, o)), x, y, z);
        }


        density -= (double)y / 20;

        if (density <= 0) {
            if (y < - 10) {
                return BlockType.WATER;
            }

            return BlockType.AIR;
        }

        return BlockType.GRASS;
        */

    }
}
