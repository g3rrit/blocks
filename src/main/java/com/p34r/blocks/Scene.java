package com.p34r.blocks;

import java.util.*;

public class Scene {

    private Map<String, Mesh> meshMap;
    private Projection projection;
    private Camera camera;
    private SkyBox skyBox;
    private TextureCache textureCache;
    private Map<String, Model> modelMap;
    private DebugGui gui;

    public Scene(int width, int height) {
        this.meshMap = new HashMap<>();

        this.projection = new Projection(width, height);
        this.camera = new Camera();
        this.textureCache = new TextureCache();
        this.modelMap = new HashMap<>();
        this.gui = new DebugGui(this);

        textureCache.create("res/textures/cube.png");
    }

    public void addMesh(String meshId, Mesh mesh) {
        meshMap.put(meshId, mesh);
    }

    public void addEntity(Entity entity) {
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException("Could not find model [" + modelId + "]");
        }
        model.getEntitiesList().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getId(), model);
    }

    public void cleanup() {
        modelMap.values().forEach(Model::cleanup);
        meshMap.values().forEach(Mesh::cleanup);
        textureCache.cleanup();
    }

    public void resize(int width, int height) {
        projection.updateProjMatrix(width, height);
    }

    // GETTERS/SETTERS

    public Map<String, Mesh> getMeshMap() {
        return meshMap;
    }

    public Projection getProjection() {
        return projection;
    }

    public Camera getCamera() {
        return camera;
    }

    public Gui getGui() {
        return gui;
    }

    public TextureCache getTextureCache() {
        return textureCache;
    }

    public Map<String, Model> getModelMap() {
        return modelMap;
    }

    public void setSkyBox(SkyBox skyBox) {
        this.skyBox = skyBox;
    }

    public SkyBox getSkyBox() {
        return skyBox;
    }
}
