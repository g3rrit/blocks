#version 330

const float SPECULAR_POWER = 10;

const int NUM_CASCADES = 3;
const float BIAS = 0.0005;
const float SHADOW_FACTOR = 0.25;
const float DEPTH_FACTOR = 0.25;

const float DIFFUSE_INTENSITY = 0.2;
const float REFLECTANCE = 10;

in vec3 outPosition;
in vec3 outNormal;
in vec2 outTextCoord;
in vec4 outWorldPosition;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};
struct AmbientLight
{
    float factor;
    vec3 color;
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
uniform AmbientLight ambientLight;
uniform DirLight dirLight;
uniform Fog fog;
uniform float time;

uniform CascadeShadow cascadeshadows[NUM_CASCADES];
uniform sampler2D shadowMap[NUM_CASCADES];

uniform sampler2D reflectionTexture;
uniform sampler2D refractionTexture;
uniform sampler2D dudv;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

vec4 calcAmbient(AmbientLight ambientLight, vec4 ambient) {
    return vec4(ambientLight.factor * ambientLight.color, 1) * ambient;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, vec3 lightColor, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuse * vec4(lightColor, 1.0) * light_intensity * diffuseFactor * DIFFUSE_INTENSITY;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specColor = specular * light_intensity  * specularFactor * REFLECTANCE; //* vec4(lightColor, 1.0);

    return diffuseColor + specColor;
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

float shadowTextureProj(vec4 shadowCoord, vec2 offset, int idx) {
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
    shadow = shadowTextureProj(shadowCoord, vec2(0, 0), idx);
    return shadow;
}

const float WATER_AMPLITUDE = 0.08;
const float WATER_WAVELENGTH = 4;
const float WATER_PHASE_SPEED = 3;
const float WATER_OFFSET = -0.3;

vec3 calcNormal(vec3 pos) {
    float amp = WATER_AMPLITUDE;
    float freq = 1 / WATER_WAVELENGTH;
    float phase = WATER_PHASE_SPEED * freq;
    float sx = pos.x * freq;
    float sz = pos.z * freq;

    float dx = freq * amp * cos(sx + time * phase) +
                    freq * amp * cos(2 * sx + time * 2 * phase) +
                        freq * amp * cos(4 * sx + time * -4 * phase);

    float dz = freq * amp * cos(sz + time * phase) +
                    freq * amp * cos(2 * sz + time * -2 * phase) +
                        freq * amp * cos(4 * sz + time * 4 * phase);

    vec3 a = vec3(1 , dx, 0);
    vec3 b = vec3(0 , dz, 1);
    return cross(normalize(b), normalize(a));
 }

void main() {
    vec4 reflectionColor = texture(reflectionTexture, outTextCoord);
    vec4 refractionColor = texture(refractionTexture, outTextCoord);
    vec4 text_color = texture(txtSampler, outTextCoord);
    vec4 ambient = calcAmbient(ambientLight, text_color);
    vec4 diffuse = vec4(0.4, 0.4, 1, 1);
    vec4 specular = vec4(1, 1, 1, 1);

    vec3 normal = calcNormal(outWorldPosition.xyz);

    vec4 diffuseSpecularComp = calcDirLight(diffuse, specular, dirLight, outPosition, normal);

    int cascadeIndex = 0;
    for (int i=0; i<NUM_CASCADES - 1; i++) {
        if (outPosition.z < cascadeshadows[i].splitDistance) {
            cascadeIndex = i + 1;
        }
    }
    float shadowFactor = calcShadow(outWorldPosition, cascadeIndex);

    fragColor = ambient + diffuseSpecularComp;
    fragColor.rgb = fragColor.rgb * shadowFactor;

    if (fog.activeFog == 1) {
        fragColor = calcFog(outPosition, fragColor, fog, ambientLight.color, dirLight);
    }

    fragColor.a = 0.85;
}