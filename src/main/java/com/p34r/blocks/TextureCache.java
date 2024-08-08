package com.p34r.blocks;

import java.util.*;

public class TextureCache {

    public static final String DEFAULT_TEXTURE = "res/textures/default_texture.png";

    private Map<String, Texture> textureMap;

    public TextureCache() {
        textureMap = new HashMap<>();
        textureMap.put(DEFAULT_TEXTURE, new Texture(DEFAULT_TEXTURE));
    }

    public void cleanup() {
        textureMap.values().forEach(Texture::cleanup);
    }

    public void create(String texturePath) {
        textureMap.computeIfAbsent(texturePath, Texture::new);
    }

    public Texture get(String texturePath) {
        Texture texture = null;
        if (texturePath != null) {
            texture = textureMap.get(texturePath);
        }
        if (texture == null) {
            texture = textureMap.get(DEFAULT_TEXTURE);
        }
        return texture;
    }
}
