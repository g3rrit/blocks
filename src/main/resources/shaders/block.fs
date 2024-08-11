#version 330

out vec4 fragColor;

uniform sampler2D txtSampler;

in vec2 outTextCoord;

void main() {
    fragColor = texture(txtSampler, outTextCoord);
}