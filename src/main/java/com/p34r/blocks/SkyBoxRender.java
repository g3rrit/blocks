package com.p34r.blocks;

import org.joml.Matrix4f;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class SkyBoxRender {

    private ShaderProgram shaderProgram;

    private UniformsMap uniformsMap;

    private Matrix4f viewMatrix;

    public SkyBoxRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/skybox.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("/shaders/skybox.fs", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        viewMatrix = new Matrix4f();
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");
    }

    public void render(Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox == null) {
            return;
        }
        shaderProgram.bind();

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        viewMatrix.set(scene.getCamera().getViewMatrix());
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        uniformsMap.setUniform("viewMatrix", viewMatrix);
        uniformsMap.setUniform("txtSampler", 0);

        Model skyBoxModel = skyBox.getSkyBoxModel();
        Entity skyBoxEntity = skyBox.getSkyBoxEntity();
        TextureCache textureCache = scene.getTextureCache();
        for (Material material : skyBoxModel.getMaterialList()) {
            Texture texture = textureCache.get(material.getTexturePath());
            glActiveTexture(GL_TEXTURE0);
            texture.bind();

            for (ObjMesh objMesh : material.getMeshList()) {
                glBindVertexArray(objMesh.getVaoId());

                uniformsMap.setUniform("modelMatrix", skyBoxEntity.getModelMatrix());
                glDrawElements(GL_TRIANGLES, objMesh.getNumVertices(), GL_UNSIGNED_INT, 0);
            }
        }

        glBindVertexArray(0);

        shaderProgram.unbind();
    }
}
