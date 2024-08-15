package com.p34r.blocks;

import org.joml.*;

public class Camera {

    private Vector3f direction;
    private Vector3f position;
    private Vector3f right;
    private Vector2f rotation;
    private Vector3f up;
    private Matrix4f viewMatrix;

    public Camera() {
        direction = new Vector3f();
        right = new Vector3f();
        up = new Vector3f();
        position = new Vector3f();
        viewMatrix = new Matrix4f();
        rotation = new Vector2f();
    }

    public synchronized void addRotation(float x, float y) {
        rotation.add(x, y);
        recalculate();
    }

    public synchronized Vector3f getPosition() {
        return position;
    }

    public synchronized Vector3f getPositionC() {
        return new Vector3f(position.x, position.y, position.z);
    }

    public synchronized Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public synchronized void moveBackwards(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.sub(direction);
        recalculate();
    }

    public synchronized void moveDown(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.sub(up);
        recalculate();
    }

    public synchronized void moveForward(float inc) {
        viewMatrix.positiveZ(direction).negate().mul(inc);
        position.add(direction);
        recalculate();
    }

    public synchronized void moveLeft(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.sub(right);
        recalculate();
    }

    public synchronized void moveRight(float inc) {
        viewMatrix.positiveX(right).mul(inc);
        position.add(right);
        recalculate();
    }

    public synchronized void moveUp(float inc) {
        viewMatrix.positiveY(up).mul(inc);
        position.add(up);
        recalculate();
    }

    private synchronized void recalculate() {
        viewMatrix.identity()
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .translate(-position.x, -position.y, -position.z);
    }

    public synchronized void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        recalculate();
    }

    public synchronized void setRotation(float x, float y) {
        rotation.set(x, y);
        recalculate();
    }
}
