package com.p34r.blocks;

import org.joml.Vector2f;
import org.tinylog.Logger;

import static org.lwjgl.glfw.GLFW.*;

public class App {

    private final long nanosInSecons = 1000000000;

    private Window window;
    private Scene scene;
    private Render render;

    float[] positions = new float[]{
            // V0
            -0.5f, 0.5f, 0.5f,
            // V1
            -0.5f, -0.5f, 0.5f,
            // V2
            0.5f, -0.5f, 0.5f,
            // V3
            0.5f, 0.5f, 0.5f,
            // V4
            -0.5f, 0.5f, -0.5f,
            // V5
            0.5f, 0.5f, -0.5f,
            // V6
            -0.5f, -0.5f, -0.5f,
            // V7
            0.5f, -0.5f, -0.5f,

            // For text coords in top face
            // V8: V4 repeated
            -0.5f, 0.5f, -0.5f,
            // V9: V5 repeated
            0.5f, 0.5f, -0.5f,
            // V10: V0 repeated
            -0.5f, 0.5f, 0.5f,
            // V11: V3 repeated
            0.5f, 0.5f, 0.5f,

            // For text coords in right face
            // V12: V3 repeated
            0.5f, 0.5f, 0.5f,
            // V13: V2 repeated
            0.5f, -0.5f, 0.5f,

            // For text coords in left face
            // V14: V0 repeated
            -0.5f, 0.5f, 0.5f,
            // V15: V1 repeated
            -0.5f, -0.5f, 0.5f,

            // For text coords in bottom face
            // V16: V6 repeated
            -0.5f, -0.5f, -0.5f,
            // V17: V7 repeated
            0.5f, -0.5f, -0.5f,
            // V18: V1 repeated
            -0.5f, -0.5f, 0.5f,
            // V19: V2 repeated
            0.5f, -0.5f, 0.5f,
    };
    float[] textCoords = new float[]{
            0.0f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.5f, 0.0f,

            0.0f, 0.0f,
            0.5f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,

            // For text coords in top face
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.0f, 1.0f,
            0.5f, 1.0f,

            // For text coords in right face
            0.0f, 0.0f,
            0.0f, 0.5f,

            // For text coords in left face
            0.5f, 0.0f,
            0.5f, 0.5f,

            // For text coords in bottom face
            0.5f, 0.0f,
            1.0f, 0.0f,
            0.5f, 0.5f,
            1.0f, 0.5f,
    };
    int[] indices = new int[]{
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top Face
            8, 10, 11, 9, 8, 11,
            // Right face
            12, 13, 7, 5, 12, 7,
            // Left face
            14, 6, 15, 6, 14, 4,
            // Bottom face
            19, 18, 16, 19, 16, 17,
            // Back face
            7, 6, 4, 7, 4, 5,
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


        Gui gui = scene.getGui();
        if (gui != null) {
            gui.inputGui(scene, window);
        }

        input(dt);
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

        Mesh mesh = new Mesh(app.positions, app.textCoords, app.indices);
        app.scene.addMesh("quad", mesh);

        app.run();
        app.cleanup();
    }

}
