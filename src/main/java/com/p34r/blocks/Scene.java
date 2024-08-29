package com.p34r.blocks;

import org.joml.Vector3f;

import java.util.*;

// TODO: Rename to Game

public class Scene {

    private Map<String, ObjMesh> meshMap;
    private Projection projection;
    private Camera camera;
    private SkyBox skyBox;
    private TextureCache textureCache;
    private Map<String, Model> modelMap;
    private Gui gui;
    private SceneLights sceneLights;
    private Terrain terrain;
    private Fog fog;
    private double time;

    private ChunkManager chunkManager;

    public Scene(int width, int height) {
        this.meshMap = new HashMap<>();

        this.projection = new Projection(width, height);
        this.camera = new Camera();
        this.textureCache = new TextureCache();
        this.modelMap = new HashMap<>();
        this.gui = new DebugGui(this);
        this.fog = new Fog(true, new Vector3f(0.2f, 0.2f, 0.3f), 0.010f);
        this.time = 0;

        this.chunkManager = new ChunkManager(this);

        textureCache.create("res/textures/cube.png");
        textureCache.create("res/textures/cube1.png");
        textureCache.create("res/textures/blocks.png");
        textureCache.create("res/models/cube/cube.png");

    }

    public void init() {
        this.chunkManager.start();
    }

    public void update(double dt) {
        time += dt;

        // update dirlight
        float ny = (float)Math.sin(time);
        float nz = (float)Math.cos(time);
        sceneLights.getDirLight().setDirection(new Vector3f(0.3f, ny, nz));

    }

    public void addMesh(String meshId, ObjMesh objMesh) {
        meshMap.put(meshId, objMesh);
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

    public void cleanup(){
        chunkManager.exit();
        try {
            chunkManager.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        chunkManager.cleanup();

        modelMap.values().forEach(Model::cleanup);
        meshMap.values().forEach(ObjMesh::cleanup);
        textureCache.cleanup();
    }

    public void resize(int width, int height) {
        projection.updateProjMatrix(width, height);
    }

    // GETTERS/SETTERS

    public Map<String, ObjMesh> getMeshMap() {
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

    public void setGui(Gui gui) {
        this.gui = gui;
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

    public void setSceneLights(SceneLights sceneLights) {
        this.sceneLights = sceneLights;
    }

    public SceneLights getSceneLights() {
        return this.sceneLights;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public double getTime() {
        return time;
    }
}
