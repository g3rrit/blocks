package com.p34r.blocks;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class ShadowRender {
    private ArrayList<CascadeShadow> cascadeShadows;
    private ShaderProgram shaderProgram;
    private ShadowBuffer shadowBuffer;
    private UniformsMap uniformsMap;

    public ShadowRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/shadow.vs", GL_VERTEX_SHADER));
        // test
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/shadow.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);

        shadowBuffer = new ShadowBuffer();

        cascadeShadows = new ArrayList<>();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            CascadeShadow cascadeShadow = new CascadeShadow();
            cascadeShadows.add(cascadeShadow);
        }

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
        shadowBuffer.cleanup();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("projViewMatrix");
    }

    public List<CascadeShadow> getCascadeShadows() {
        return cascadeShadows;
    }

    public ShadowBuffer getShadowBuffer() {
        return shadowBuffer;
    }

    public void render(Scene scene) {
        ChunkManager chunkManager = scene.getChunkManager();
        CascadeShadow.updateCascadeShadows(cascadeShadows, scene);

        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);

        shaderProgram.bind();

        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIds()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);

            CascadeShadow shadowCascade = cascadeShadows.get(i);
            uniformsMap.setUniform("projViewMatrix", shadowCascade.getProjViewMatrix());

            chunkManager.forEach((chunk) -> {
                uniformsMap.setUniform("modelMatrix", chunk.getModelMatrix());
                // TODO: only run for the sides that are actually needed
                for (int side = 0; side < 6; side++) {
                    if (chunk.isSideEmpty(side)) {
                        continue;
                    }

                    BlockMesh mesh = chunk.getMesh(side);
                    glBindVertexArray(mesh.getVaoId());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
            });
        }

        shaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}
