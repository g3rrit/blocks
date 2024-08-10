package com.p34r.blocks;

import org.joml.Matrix4f;

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
        shaderProgram.bind();

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        uniformsMap.setUniform("txtSampler", 0);


        Matrix4f cpos = new Matrix4f();
        cpos.translate(0, 0, 0);
        uniformsMap.setUniform("modelMatrix", cpos);
        glActiveTexture(GL_TEXTURE0);
        Texture ctexture = scene.getTextureCache().get("res/textures/cube.png");
        ctexture.bind();

        scene.getMeshMap().values().forEach(mesh -> {
                    glBindVertexArray(mesh.getVaoId());
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
        );

        glBindVertexArray(0);

        shaderProgram.unbind();
    }

    private void createUniforms() {
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");
        uniformsMap.createUniform("txtSampler");
        uniformsMap.createUniform("modelMatrix");
    }
}