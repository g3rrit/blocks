#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 textCoord;
// layout (location=2) in int blockType;

out vec2 outTextCoord;
out vec3 outPosition;
out vec3 outNormal;
out vec4 outWorldPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 sideNormal;
uniform float time;

const float WATER_AMPLITUDE = 0.08;
const float WATER_WAVELENGTH = 4;
const float WATER_PHASE_SPEED = 3;
const float WATER_OFFSET = -0.3;

float calcWaterHeight(float x, float z) {
    float amp = WATER_AMPLITUDE;
    float freq = 1 / WATER_WAVELENGTH;
    float phase = WATER_PHASE_SPEED * freq;
    float sx = x * freq;
    float sz = z * freq;
    return (
        amp * sin(sx + time * phase) +
        amp * sin(sz + time * phase) +
            0.5 * amp * sin(2 * sx + time * 2 * phase) +
            0.5 * amp * sin(2 * sz + time * -2 * phase) +
                        0.25 * amp * sin(4 * sx + time * -4 * phase) +
                        0.25 * amp * sin(4 * sz + time * 4 * phase) +
        WATER_OFFSET
    );
}

void main()
{
    vec4 m_pos = modelMatrix * vec4(position, 1.0);
    m_pos.y += calcWaterHeight(m_pos.x, m_pos.z);
    //mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vec4 mvPosition = viewMatrix * m_pos;
    gl_Position = projectionMatrix * mvPosition;
    outTextCoord = textCoord;
    outPosition = mvPosition.xyz;
    outNormal = sideNormal;
    outWorldPosition = m_pos;
}
