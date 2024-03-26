/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#version 330

#include UI_SCALING_MODE

#define SAMPLING_MITCHELL 1
#define SAMPLING_CATROM 2
#define SAMPLING_XBR 3
#define TRANSPARENCY_COLOR_PACKED 12345678

uniform sampler2D uiTexture;
uniform sampler2D minimapMask;
uniform sampler2D minimapImage;

uniform int samplingMode;
uniform ivec2 sourceDimensions;
uniform ivec2 targetDimensions;
uniform float colorBlindnessIntensity;
uniform vec4 alphaOverlay;
uniform float minimapEnabled;
uniform ivec2 minimapLocation;
uniform ivec2 minimapPlayerLocation;

const ivec3 TRANSPARENCY_COLOR = ivec3(
    TRANSPARENCY_COLOR_PACKED >> 16 & 0xFF,
    TRANSPARENCY_COLOR_PACKED >> 8 & 0xFF,
    TRANSPARENCY_COLOR_PACKED & 0xFF
);

// Function to replace transparency with black
vec4 replaceTransparency(vec4 c) {
    return ivec3(round(c.rgb * 0xFF)) == TRANSPARENCY_COLOR ? vec4(0) : c;
}

#include scaling/bicubic.glsl
#include utils/constants.glsl
#include utils/color_blindness.glsl

#if SHADOW_MAP_OVERLAY
uniform sampler2D shadowMap;
uniform ivec4 shadowMapOverlayDimensions;
#endif

#if UI_SCALING_MODE == SAMPLING_XBR
#include scaling/xbr_lv2_frag.glsl

in XBRTable xbrTable;
#endif

in vec2 TexCoord;

out vec4 FragColor;

vec4 alphaBlend(vec4 src, vec4 dst) {
    return vec4(
        src.rgb + dst.rgb * (1.0f - src.a),
        src.a + dst.a * (1.0f - src.a)
    );
}

// Function to get minimap location in screen space
ivec2 getMinimapLocation() {
    return ivec2(minimapLocation.x, sourceDimensions.y - minimapLocation.y - 152);
}

vec4 applyMinimapOverlay(vec4 originalColor);

void main() {
    #if SHADOW_MAP_OVERLAY
    {
        vec2 uv = (gl_FragCoord.xy - shadowMapOverlayDimensions.xy) / shadowMapOverlayDimensions.zw;
        if (0 <= uv.x && uv.x <= 1 && 0 <= uv.y && uv.y <= 1) {
            FragColor = texture(shadowMap, uv);
            return;
        }
    }
    #endif

    #if UI_SCALING_MODE == SAMPLING_MITCHELL || UI_SCALING_MODE == SAMPLING_CATROM
    vec4 c = textureCubic(uiTexture, TexCoord);
    #elif UI_SCALING_MODE == SAMPLING_XBR
    vec4 c = textureXBR(uiTexture, TexCoord, xbrTable, ceil(1.0 * targetDimensions.x / sourceDimensions.x));
    #else // NEAREST or LINEAR, which uses GL_TEXTURE_MIN_FILTER/GL_TEXTURE_MAG_FILTER to affect sampling
    vec4 c = texture(uiTexture, TexCoord);
    c = replaceTransparency(c);
    #endif

    if(minimapEnabled == 0) {
        c = applyMinimapOverlay(c);
    }

    c = alphaBlend(c, alphaOverlay);
    c.rgb = colorBlindnessCompensation(c.rgb);

    FragColor = c;
}

vec4 applyMinimapOverlay(vec4 originalColor) {
    ivec2 minimapArea = textureSize(minimapImage, 0);

    int minimapCircleDiameter = 152;

    ivec2 minimapTopRight = getMinimapLocation();

    ivec2 screenPos = ivec2(floor(gl_FragCoord.xy + 0.5));

    // Check if the current fragment is within the circular minimap area at the top right of the screen
    if (distance(vec2(screenPos), vec2(minimapTopRight) + vec2(minimapCircleDiameter) / 2.0) <= minimapCircleDiameter / 2.0) {
        ivec2 relativeScreenPos = screenPos - (minimapTopRight + ivec2(minimapCircleDiameter) / 2);
        vec2 minimapTexCoords = (vec2(minimapPlayerLocation.x + 1, minimapPlayerLocation.y + 1) + vec2(relativeScreenPos)) / vec2(minimapArea);
        minimapTexCoords = minimapTexCoords + (0.5 / vec2(minimapArea));
        vec4 minimapColor = texture(minimapImage, minimapTexCoords);

        // Applying mask inversion
        ivec2 textureCoordInt = ivec2((gl_FragCoord.xy - minimapTopRight));
        textureCoordInt = ivec2(clamp(textureCoordInt.x, 0, textureSize(minimapMask, 0).x - 1),clamp(textureCoordInt.y, 0, textureSize(minimapMask, 0).y - 1));
        vec4 minimapMaskColor = texelFetch(minimapMask, textureCoordInt, 0);
        originalColor = alphaBlend(originalColor, minimapColor * (1.0 - minimapMaskColor.a));

        return originalColor;
    }

    // Outside the minimap area, return the original color
    return originalColor;
}