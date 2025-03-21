package com.p34r.blocks;

import org.lwjgl.opengl.GL;

import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;

public class Render {

    // TODO: rename to object render or something
    private SceneRender sceneRender;
    private GuiRender guiRender;
    private SkyBoxRender skyBoxRender;
    private BlockRender blockRender;
    private WaterRender waterRender;
    private ShadowRender shadowRender;
    private CameraDepthRender cameraDepthRender;
    private WaterRefRender waterRefRender;

    public Render(Window window) {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        //glEnable(GL_MULTISAMPLE);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        sceneRender = new SceneRender();
        guiRender = new GuiRender(window);
        skyBoxRender = new SkyBoxRender();
        blockRender = new BlockRender();
        shadowRender = new ShadowRender();
        waterRender = new WaterRender();
        cameraDepthRender = new CameraDepthRender();
        waterRefRender = new WaterRefRender();
    }

    public void cleanup() {
        sceneRender.cleanup();
        guiRender.cleanup();
        skyBoxRender.cleanup();
        shadowRender.cleanup();
        waterRender.cleanup();
    }

    public void render(Window window, Scene scene) {
        ChunkManager chunkManager = scene.getChunkManager();
        chunkManager.gc();

        //waterRefRender.render(scene);

        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        cameraDepthRender.render(scene);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        shadowRender.render(scene);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        skyBoxRender.render(scene);
        blockRender.render(scene, shadowRender);
        waterRender.render(scene, shadowRender);
        //sceneRender.render(scene);
        //guiRender.render(scene);
    }

    public void resize(int width, int height) {
        guiRender.resize(width, height);
    }
}
