package com.p34r.blocks;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;

public class CameraDepthRender {

    private ShaderProgram shaderProgram;
    private CameraDepthBuffer cameraDepthBuffer;
    private UniformsMap uniformsMap;

    public CameraDepthRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/camdepth.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/camdepth.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);

        cameraDepthBuffer = new CameraDepthBuffer();

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        cameraDepthBuffer.cleanup();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
    }

    public CameraDepthBuffer getCameraDepthBuffer() {
        return cameraDepthBuffer;
    }

    public void render(Scene scene) {
        ChunkManager chunkManager = scene.getChunkManager();

        glBindFramebuffer(GL_FRAMEBUFFER, cameraDepthBuffer.getDepthMapFBO());
        glViewport(0, 0, CameraDepthBuffer.DEPTH_MAP_WIDTH, CameraDepthBuffer.DEPTH_MAP_HEIGHT);

        shaderProgram.bind();

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, cameraDepthBuffer.getTextId(), 0);
        glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_ALPHA_TEST);

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

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

        shaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDisable(GL_ALPHA_TEST);
    }
}
