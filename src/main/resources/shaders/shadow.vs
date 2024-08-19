#version 330

layout (location=0) in vec3 position;

uniform mat4 modelMatrix;
uniform mat4 projViewMatrix;

void main()
{
    gl_Position = projViewMatrix * modelMatrix * vec4(position, 1.0);
    //gl_Position = modelMatrix * vec4(position, 1.0);
}