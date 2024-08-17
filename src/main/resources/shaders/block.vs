#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 textCoord;

out vec2 outTextCoord;
out vec3 outPosition;
out vec3 outNormal;
out vec4 outWorldPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform vec3 sideNormal;


void main()
{
    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vec4 mvPosition =  modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPosition;
    outTextCoord = textCoord;
    outPosition = mvPosition.xyz;
    outNormal = sideNormal;
    outWorldPosition = modelMatrix * vec4(position, 1.0);
}
