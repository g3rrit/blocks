package com.p34r.blocks;

import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeListener;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class WaterRefRender {

    private ShaderProgram shaderProgram;
    private WaterRefBuffer waterRefBuffer;
    private UniformsMap uniformsMap;

    public WaterRefRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/waterref.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/waterref.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);

        waterRefBuffer = new WaterRefBuffer();

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        waterRefBuffer.cleanup();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("clipPlane");
    }

    public WaterRefBuffer getWaterRefBuffer() {
        return waterRefBuffer;
    }

    public void renderScene(Scene scene) {
        ChunkManager chunkManager = scene.getChunkManager();
        try (ChunkContainer chunkContainer = chunkManager.getChunkContainer()) {
            for (Chunk chunk: chunkContainer.getAll()) {
                uniformsMap.setUniform("modelMatrix", chunk.getModelMatrix());
                // TODO: only run for the sides that are actually needed
                for (int side = 0; side < 6; side++) {
                    if (chunk.isSideEmpty(BlockMaterial.SOLID, side)) {
                        continue;
                    }
                    if (side == Side.BOTTOM) {
                        continue;
                    }

                    BlockMesh mesh = chunk.getMesh(BlockMaterial.SOLID, side);
                    glBindVertexArray(mesh.getVaoId());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void render(Scene scene) {
        int waterHeight = -10;

        shaderProgram.bind();

        waterRefBuffer.bindRefractionFrameBuffer();
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("clipPlane", new Vector4f(0, -1, 0, waterHeight));

        renderScene(scene);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        waterRefBuffer.bindReflectionFrameBuffer();
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        uniformsMap.setUniform("clipPlane", new Vector4f(0, 1, 0, -waterHeight));

        renderScene(scene);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        shaderProgram.unbind();
    }
}
