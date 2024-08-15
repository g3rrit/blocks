package com.p34r.blocks;

import java.util.*;

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

    private ChunkManager chunkManager;

    public Scene(int width, int height) {
        this.meshMap = new HashMap<>();

        this.projection = new Projection(width, height);
        this.camera = new Camera();
        this.textureCache = new TextureCache();
        this.modelMap = new HashMap<>();
        this.gui = new DebugGui(this);

        this.chunkManager = new ChunkManager(this);

        /*
        this.chunks = new ArrayList<>();
        this.terrain = new Terrain();

        // temp
        for (int x = -5; x < 5; x++) {
            for (int y = -5; y < 5; y++) {
                for (int z = -5; z < 5; z++) {
                    this.chunks.add(new Chunk(terrain, x, y, z));
                }
            }
        }
        */

        textureCache.create("res/textures/cube.png");
        textureCache.create("res/textures/cube1.png");
        textureCache.create("res/textures/blocks.png");
        textureCache.create("res/models/cube/cube.png");

    }

    public void init() {
        this.chunkManager.start();
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

}
