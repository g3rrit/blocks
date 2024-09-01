package com.p34r.blocks;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL30.*;

public class CameraDepthBuffer {
    public static final int DEPTH_MAP_WIDTH = 4096;
    public static final int DEPTH_MAP_HEIGHT = DEPTH_MAP_WIDTH;

    private final int depthMapFBO;
    private final int textId;

    public CameraDepthBuffer() {
        depthMapFBO = glGenFramebuffers();

        textId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Attach the depth map texture to the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, textId, 0);

        // Set only depth
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FrameBuffer");
        }

        // Unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bindTexture(int i) {
        glActiveTexture(i);
        glBindTexture(GL_TEXTURE_2D, textId);
    }

    public void cleanup() {
        glDeleteFramebuffers(depthMapFBO);
        glDeleteTextures(textId);
    }

    public int getDepthMapFBO() {
        return depthMapFBO;
    }

    public int getTextId() {
        return textId;
    }

}
