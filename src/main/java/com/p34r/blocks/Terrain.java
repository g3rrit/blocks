package com.p34r.blocks;

public class Terrain {

    private int seed = 42;

    public BlockType get(int x, int y, int z) {
        int scale = 1;
        double res = SimplexNoise.noise((float)x / 100, (float)z / 100) * 100;
        //System.out.println(res);

        if (y > res) {
            return BlockType.AIR;
        }
        return BlockType.DIRT;
    }
}
