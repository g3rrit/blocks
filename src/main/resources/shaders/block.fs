#version 330

out vec4 fragColor;

in vec4 outColor;

void main() {
    fragColor = outColor;
}