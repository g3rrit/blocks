package com.p34r.blocks;

import org.joml.Matrix4f;
import org.joml.Vector3f;

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

    public void render(Scene scene) {
        ChunkManager chunkManager = scene.getChunkManager();

        // TODO: do this somewhere else
        chunkManager.gc();

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shaderProgram.bind();

        Fog fog = scene.getFog();
        uniformsMap.setUniform("fog.activeFog", fog.isActive() ? 1 : 0);
        uniformsMap.setUniform("fog.color", fog.getColor());
        uniformsMap.setUniform("fog.density", fog.getDensity());

        SceneLights.updateLights(scene, uniformsMap);

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        glActiveTexture(GL_TEXTURE0);
        Texture ctexture = scene.getTextureCache().get("res/textures/blocks.png");
        ctexture.bind();

        for (int side = 0; side < 6; side++) {
            int finalSide = side;
            uniformsMap.setUniform("sideNormal", Side.getNormal(side));

            chunkManager.forEach((chunk) -> {
                if (chunk.isSideEmpty(finalSide)) {
                    return;
                }

                uniformsMap.setUniform("modelMatrix", chunk.getModelMatrix());
                BlockMesh mesh = chunk.getMesh(finalSide);
                glBindVertexArray(mesh.getVaoId());
                glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
            });
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("modelMatrix");

        uniformsMap.createUniform("fog.activeFog");
        uniformsMap.createUniform("fog.color");
        uniformsMap.createUniform("fog.density");

        uniformsMap.createUniform("sideNormal");

        SceneLights.createUniforms(uniformsMap);


    }
}