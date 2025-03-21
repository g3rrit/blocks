package com.p34r.blocks;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class BlockRender {

    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public BlockRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/block.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/block.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);

        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    public void render(Scene scene, ShadowRender shadowRender) {
        ChunkManager chunkManager = scene.getChunkManager();

        shaderProgram.bind();

        Fog fog = scene.getFog();
        uniformsMap.setUniform("fog.activeFog", fog.isActive() ? 1 : 0);
        uniformsMap.setUniform("fog.color", fog.getColor());
        uniformsMap.setUniform("fog.density", fog.getDensity());

        SceneLights.updateLights(scene, uniformsMap);

        uniformsMap.setUniform("txtSampler", 0);
        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        glActiveTexture(GL_TEXTURE0);
        Texture ctexture = scene.getTextureCache().get("res/textures/blocks.png");
        ctexture.bind();

        int start = 2;
        List<CascadeShadow> cascadeShadows = shadowRender.getCascadeShadows();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            uniformsMap.setUniform("shadowMap[" + i + "]", start + i);
            CascadeShadow cascadeShadow = cascadeShadows.get(i);
            uniformsMap.setUniform("cascadeshadows[" + i + "]" + ".projViewMatrix", cascadeShadow.getProjViewMatrix());
            uniformsMap.setUniform("cascadeshadows[" + i + "]" + ".splitDistance", cascadeShadow.getSplitDistance());
        }

        shadowRender.getShadowBuffer().bindTextures(GL_TEXTURE2);

        for (int side = 0; side < 6; side++) {

            uniformsMap.setUniform("sideNormal", Side.getNormal(side));

            try (ChunkContainer chunkContainer = chunkManager.getChunkContainer()) {
                for (Chunk chunk: chunkContainer.getAll()) {
                    if (chunk.isSideEmpty(BlockMaterial.SOLID, side)) {
                        continue;
                    }

                    uniformsMap.setUniform("modelMatrix", chunk.getModelMatrix());
                    BlockMesh mesh = chunk.getMesh(BlockMaterial.SOLID, side);
                    glBindVertexArray(mesh.getVaoId());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());

        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");

        uniformsMap.createUniform("fog.activeFog");
        uniformsMap.createUniform("fog.color");
        uniformsMap.createUniform("fog.density");

        uniformsMap.createUniform("sideNormal");

        SceneLights.createUniforms(uniformsMap);

        // shadows
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            uniformsMap.createUniform("shadowMap[" + i + "]");
            uniformsMap.createUniform("cascadeshadows[" + i + "]" + ".projViewMatrix");
            uniformsMap.createUniform("cascadeshadows[" + i + "]" + ".splitDistance");
        }
    }
}