#version 330

layout (location=0) in vec3 position;
//layout (location=1) in int color;
//layout (location=2) in int normalDir;

out vec4 outColor;
//out vec3 outNormal;
//out vec3 outPosition;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;


void main()
{
    int color = 0xff000000;
    float color_r = float(color >> 24 & 0xff);
    float color_g = float(color >> 16 & 0xff);
    float color_b = float(color >> 8  & 0xff);
    float color_a = color       & 0xff;

    //float normal_x = 1.0 * (normalDir & 1) - 1.0 * (normalDir >> 1 & 1);
    //float normal_y = 1.0 * (normalDir >> 2 & 1) * -1.0 * (normalDir >> 3 & 1);
    //float normal_z = 1.0 * (normalDir >> 4 & 1) * -1.0 * (normalDir >> 5 & 1);

    mat4 modelViewMatrix = viewMatrix * modelMatrix;
    vec4 mvPosition =  modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPosition;
    //outPosition = mvPosition.xyz;
    outColor = vec4(color_r, color_g, color_b, color_a);
    //outNormal = normalize(modelViewMatrix * vec4(normal_x, normal_y, normal_z, 0.0)).xyz;
}
