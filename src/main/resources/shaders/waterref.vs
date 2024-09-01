#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 textCoord;

out vec2 outTextCoord;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec4 clipPlane;

void main()
{
    vec4 worldPosition =  viewMatrix * modelMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * worldPosition;
    outTextCoord = textCoord;
    gl_ClipDistance[0] = dot(worldPosition, clipPlane);
}