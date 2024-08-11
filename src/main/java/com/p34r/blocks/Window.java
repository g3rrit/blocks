package com.p34r.blocks;

import org.lwjgl.glfw.*;

import java.util.concurrent.Callable;
import org.tinylog.Logger;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final long windowHandle;
    private int height;
    private int width;
    private Callable<Void> resizeFunc;
    private MouseInput mouseInput;

    public Window(Callable<Void> resizeFunc) {
        this.resizeFunc = resizeFunc;

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (Defs.COMPATIBILITY_PROFILE) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        if (Defs.INIT_WINDOW_WIDTH > 0 && Defs.INIT_WINDOW_HEIGHT > 0) {
            this.width = Defs.INIT_WINDOW_WIDTH;
            this.height = Defs.INIT_WINDOW_HEIGHT;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = vidMode.width();
            height = vidMode.height();
        }

        windowHandle = glfwCreateWindow(width, height, Defs.WINDOW_TITLE, NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resized(w, h));

        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            keyCallBack(key, action);
        });

        glfwMakeContextCurrent(windowHandle);

        glfwSwapInterval(0);

        glfwShowWindow(windowHandle);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
        width = arrWidth[0];
        height = arrHeight[0];

        mouseInput = new MouseInput(windowHandle);
    }

    public void cleanup() {
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    public void keyCallBack(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(windowHandle, true); // We will detect this in the rendering loop
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    protected void resized(int width, int height) {
        this.width = width;
        this.height = height;
        try {
            resizeFunc.call();
        } catch (Exception excp) {
            Logger.error("Error calling resize callback", excp);
        }
    }

    public void update() {
        glfwSwapBuffers(windowHandle);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }

    // GETTERS/SETTERS

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }
}
