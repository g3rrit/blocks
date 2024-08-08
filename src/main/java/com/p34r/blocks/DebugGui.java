package com.p34r.blocks;

import org.joml.Vector3f;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiWindowFlags;
import imgui.flag.ImGuiCond;
import org.joml.Vector2f;

public class DebugGui implements Gui {

    private Scene scene;

    public DebugGui(Scene scene) {
        this.scene = scene;
    }

    @Override
    public void drawGui() {
        Camera camera = scene.getCamera();
        Vector3f camPos = camera.getPosition();

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setWindowSize(200, 200);
        ImGui.begin("Debug", ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoInputs);
        ImGui.text("FPS: " + DebugState.fps);
        ImGui.text("POS: " + (int)camPos.x + " - " + (int) camPos.y + " - " + (int) camPos.z);
        ImGui.end();
        ImGui.endFrame();
        ImGui.render();
    }

    @Override
    public boolean inputGui(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.setMousePos(mousePos.x, mousePos.y);
        imGuiIO.setMouseDown(0, mouseInput.isLeftButtonPressed());
        imGuiIO.setMouseDown(1, mouseInput.isRightButtonPressed());

        return imGuiIO.getWantCaptureMouse() || imGuiIO.getWantCaptureKeyboard();
    }
}
