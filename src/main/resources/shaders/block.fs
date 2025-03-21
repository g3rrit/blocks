#version 330

const int MAX_POINT_LIGHTS = 5;
const float SPECULAR_POWER = 10;

const int DEBUG_SHADOWS = 0;

const int NUM_CASCADES = 3;
const float BIAS = 0.0005;
const float SHADOW_FACTOR = 0.25;

in vec3 outPosition;
in vec3 outNormal;
in vec2 outTextCoord;
in vec4 outWorldPosition;
flat in int outBlockType;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};
struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float reflectance;
};
struct AmbientLight
{
    float factor;
    vec3 color;
};
struct PointLight {
    vec3 position;
    vec3 color;
    float intensity;
    Attenuation att;
};

struct DirLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

struct Fog
{
    int activeFog;
    vec3 color;
    float density;
};

struct CascadeShadow {
    mat4 projViewMatrix;
    float splitDistance;
};

uniform sampler2D txtSampler;
uniform Material material;
uniform AmbientLight ambientLight;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform DirLight dirLight;
uniform Fog fog;

uniform CascadeShadow cascadeshadows[NUM_CASCADES];
uniform sampler2D shadowMap[NUM_CASCADES];

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient) {
    return vec4(ambientLight.factor * ambientLight.color, 1) * ambient;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, vec3 lightColor, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    //vec3 camera_direction = normalize(-position);
    //vec3 from_light_dir = -to_light_dir;
    //vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    //float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    //specularFactor = pow(specularFactor, SPECULAR_POWER);
    //specColor = specular * light_intensity  * specularFactor * material.reflectance * vec4(lightColor, 1.0);

    return diffuseColor;//(diffuseColor + specColor);
}

vec4 calcPointLight(vec4 diffuse, vec4 specular, PointLight light, vec3 position, vec3 normal) {
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_color = calcLightColor(diffuse, specular, light.color, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
    light.att.exponent * distance * distance;
    return light_color / attenuationInv;
}

vec4 calcDirLight(vec4 diffuse, vec4 specular, DirLight light, vec3 position, vec3 normal) {
    return calcLightColor(diffuse, specular, light.color, light.intensity, position, normalize(light.direction), normal);
}

vec4 calcFog(vec3 pos, vec4 color, Fog fog, vec3 ambientLight, DirLight dirLight) {
    vec3 fogColor = fog.color * (ambientLight + dirLight.color * dirLight.intensity);
    float distance = length(pos);
    float fogFactor = 1.0 / exp((distance * fog.density) * (distance * fog.density));
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    vec3 resultColor = mix(fogColor, color.xyz, fogFactor);
    return vec4(resultColor.xyz, color.w);
}

float textureProj(vec4 shadowCoord, vec2 offset, int idx) {
     float shadow = 1.0;

     if (shadowCoord.z > -1.0 && shadowCoord.z < 1.0) {
         float dist = 0.0;
         dist = texture(shadowMap[idx], vec2(shadowCoord.xy + offset)).r;
         if (shadowCoord.w > 0 && dist < shadowCoord.z - BIAS) {
             shadow = SHADOW_FACTOR;
         }
     }
     return shadow;
 }

 float calcShadow(vec4 worldPosition, int idx) {
     vec4 shadowMapPosition = cascadeshadows[idx].projViewMatrix * worldPosition;
     float shadow = 1.0;
     vec4 shadowCoord = (shadowMapPosition / shadowMapPosition.w) * 0.5 + 0.5;
     shadow = textureProj(shadowCoord, vec2(0, 0), idx);
     return shadow;
 }

void main() {
    vec4 text_color = texture(txtSampler, outTextCoord);
    //text_color = texture(shadowMap[1], outTextCoord); //texture(txtSampler, outTextCoord);
    vec4 ambient = calcAmbient(ambientLight, text_color + material.ambient);
    vec4 diffuse = text_color;// + material.diffuse;
    vec4 specular = text_color;// + material.specular;

    vec4 diffuseSpecularComp = calcDirLight(diffuse, specular, dirLight, outPosition, outNormal);

    int cascadeIndex = 0;
    for (int i=0; i<NUM_CASCADES - 1; i++) {
        if (outPosition.z < cascadeshadows[i].splitDistance) {
            cascadeIndex = i + 1;
        }
    }
    float shadowFactor = calcShadow(outWorldPosition, cascadeIndex);

    for (int i=0; i<MAX_POINT_LIGHTS; i++) {
        if (pointLights[i].intensity > 0) {
            //diffuseSpecularComp += calcPointLight(diffuse, specular, pointLights[i], outPosition, outNormal);
        }
    }

    //fragColor = vec4(shadowFactor, 0, 0, 1);
    //fragColor = vec4(outPosition.xyz, 1); //+ diffuseSpecularComp;

    fragColor = ambient + diffuseSpecularComp;
    fragColor.rgb = fragColor.rgb * shadowFactor;

    if (fog.activeFog == 1) {
        fragColor = calcFog(outPosition, fragColor, fog, ambientLight.color, dirLight);
    }

    if (outBlockType == 3) {
        fragColor.a = 0.7;
    }

    // DEBUG
    if (DEBUG_SHADOWS == 1) {
        switch (cascadeIndex) {
            case 0:
            fragColor.rgb *= vec3(1.0f, 0.25f, 0.25f);
            break;
            case 1:
            fragColor.rgb *= vec3(0.25f, 1.0f, 0.25f);
            break;
            case 2:
            fragColor.rgb *= vec3(0.25f, 0.25f, 1.0f);
            break;
            default :
            fragColor.rgb *= vec3(1.0f, 1.0f, 0.25f);
            break;
        }
    }
}