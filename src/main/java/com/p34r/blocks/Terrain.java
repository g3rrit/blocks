package com.p34r.blocks;

import org.tinylog.Logger;

public class Terrain {

    private int seed = 42;

    private double getOctave(double length, double scale, int x, int z) {
        return SimplexNoise.noise((double)x / length, (double)z / length) * scale;
    }

    private double getDensity(double length, int x, int y, int z) {
        return SimplexNoise.noise((double)x / length, (double)y / length, (double)z / length);
    }

    public BlockType get(int x, int y, int z) {
        if (y < -20) {
            return BlockType.DIRT;
        }

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

    }
}
