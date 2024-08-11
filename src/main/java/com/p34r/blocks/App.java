package com.p34r.blocks;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.tinylog.Logger;

import static org.lwjgl.glfw.GLFW.*;

public class App {

    private final long nanosInSecons = 1000000000;

    private Window window;
    private Scene scene;
    private Render render;

    /**
     * Mesh:
     *      V3D *--------* V2C
     *         /|       /|
     *        / |V0A   / |
     *   V7B *--*-----*  * V1B
     *       | /   V6A| /
     *       |/       |/
     *   V4C *--------* V5D
     *
     * Texture:
     *     D *--------* C
     *       |        |
     *       |        |
     *     A *--------* B
     */
    float[] positions = new float[]{
            // V0
            0, 0, 0,
            // V1
            1, 0, 0,
            // V2
            1, 1, 0,
            // V3
            0, 1, 0,
            // V4
            0, 0, 1,
            // V5
            1, 0, 1,
            // V6
            1, 1, 1,
            // V7
            0, 1, 1,
            // V0
            0, 0, 1,
            // V1
            1, 0, 1,
            // V2
            1, 1, 1,
            // V3
            0, 1, 1,
            // V4
            0, 0, 2,
            // V5
            1, 0, 2,
            // V6
            1, 1, 2,
            // V7
            0, 1, 2,
    };
    float[] textCoords = new float[]{
            // A
            0, 1,
            // B
            1, 1,
            // C
            1, 0,
            // D
            0, 0,
            // C
            1, 0,
            // D
            0, 0,
            // A
            0, 1,
            // B
            1, 1,
            // A
            0, 1,
            // B
            1, 1,
            // C
            1, 0,
            // D
            0, 0,
            // C
            1, 0,
            // D
            0, 0,
            // A
            0, 1,
            // B
            1, 1,
    };
    int[] indices = new int[]{
            // Front face
            7, 4, 5, 5, 6, 7,
            // Back face
            2, 1, 0, 0, 3, 2,
            // Top face
            3, 7, 6, 6, 2, 3,
            // Bottom face
            4, 0, 1, 1, 5, 4,
            // Left face
            3, 0, 4, 4, 7, 3,
            // Right face
            6, 5, 1, 1, 2, 6,
            // Front face
            7, 4, 5, 5, 6, 7,
            // Back face
            2, 1, 0, 0, 3, 2,
            // Top face
            3, 7, 6, 6, 2, 3,
            // Bottom face
            4, 0, 1, 1, 5, 4,
            // Left face
            3, 0, 4, 4, 7, 3,
            // Right face
            6, 5, 1, 1, 2, 6,
    };

    public App() {
        this.window = new Window(() -> {
            this.resize(window.getWidth(), window.getHeight());
            return null;
        });
        this.render = new Render(window);
        this.scene = new Scene(window.getWidth(), window.getHeight());

        SkyBox skyBox = new SkyBox("res/models/skybox/skybox.obj", scene.getTextureCache());
        skyBox.getSkyBoxEntity().setScale(50);
        skyBox.getSkyBoxEntity().updateModelMatrix();
        scene.setSkyBox(skyBox);

        Model cubeModel = ModelLoader.loadModel("cube-model", "res/models/cube/cube.obj",
                scene.getTextureCache());
        scene.addModel(cubeModel);

        Entity cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(-3, 0, -2);
        cubeEntity.setRotation(1, 0, 0, 60);
        cubeEntity.updateModelMatrix();
        scene.addEntity(cubeEntity);

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.3f);
        scene.setSceneLights(sceneLights);
        sceneLights.getPointLights().add(new PointLight(new Vector3f(1, 1, 1),
                new Vector3f(0, 0, -1.4f), 1.0f));

        LightControls lightControls = new LightControls(scene);
        scene.setGui(lightControls);
    }

    private void cleanup() {
        render.cleanup();
        scene.cleanup();
        window.cleanup();
    }

    private long startFps = 0;
    private int fpsCount = 0;

    void input(double dt) {
        window.getMouseInput().input();

        float move = (float) dt * Config.movementSpeed;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.moveDown(move);
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isLeftButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            double rotX = -displVec.x * Config.mouseSensitivity;
            double rotY = -displVec.y * Config.mouseSensitivity;

            camera.addRotation((float) Math.toRadians(rotX),
                    (float) Math.toRadians(rotY));
        }
    }

    private void update(double dt) {
        fpsCount++;
        if ((System.nanoTime() - startFps) > nanosInSecons) {
            DebugState.fps = fpsCount;
            fpsCount = 0;
            startFps = System.nanoTime();
        }


        boolean inputConsumed = false;
        Gui gui = scene.getGui();
        if (gui != null) {
            inputConsumed = gui.inputGui(scene, window);
        }

        if (!inputConsumed) {
            input(dt);
        }
    }

    private void draw() {
    }

    private void run() {
        long startFrame = 0;
        long dt = 0;
        while (!window.windowShouldClose()) {
            startFrame = System.nanoTime();

            window.pollEvents();

            update((double) dt / 1000);
            dt = 0;

            render.render(window, scene);

            window.update();

            while ((dt = (System.nanoTime() - startFrame)) < (nanosInSecons / Config.fps)) {
                try {
                    Thread.sleep(1);  // TODO: find better value here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resize(int width, int height) {
        scene.resize(width, height);
        render.resize(width, height);
    }

    public static void main(String[] args) {
        Logger.info("Starting app");
        App app = new App();

        //Mesh mesh = new Mesh(app.positions, app.textCoords, app.indices);
        //app.scene.addMesh("quad", mesh);


        app.run();
        app.cleanup();
    }

}
