package com.p34r.blocks;

import org.joml.Vector4f;

import java.util.*;

public class Material {

    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private Vector4f ambientColor;
    private Vector4f diffuseColor;
    private List<ObjMesh> meshList;
    private float reflectance;
    private Vector4f specularColor;
    private String texturePath;

    public Material() {
        diffuseColor = DEFAULT_COLOR;
        ambientColor = DEFAULT_COLOR;
        specularColor = DEFAULT_COLOR;
        meshList = new ArrayList<>();
    }

    public void cleanup() {
        meshList.forEach(ObjMesh::cleanup);
    }

    public Vector4f getAmbientColor() {
        return ambientColor;
    }

    public Vector4f getDiffuseColor() {
        return diffuseColor;
    }

    public List<ObjMesh> getMeshList() {
        return meshList;
    }

    public float getReflectance() {
        return reflectance;
    }

    public Vector4f getSpecularColor() {
        return specularColor;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void setAmbientColor(Vector4f ambientColor) {
        this.ambientColor = ambientColor;
    }

    public void setDiffuseColor(Vector4f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public void setReflectance(float reflectance) {
        this.reflectance = reflectance;
    }

    public void setSpecularColor(Vector4f specularColor) {
        this.specularColor = specularColor;
    }

    public void setTexturePath(String texturePath) {
        this.texturePath = texturePath;
    }
}
