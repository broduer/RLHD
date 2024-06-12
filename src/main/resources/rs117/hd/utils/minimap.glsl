#pragma once

#include utils/constants.glsl
#include utils/fragui.glsl
#if OPENGL_MINIMAP
uniform sampler2D minimapMask;
uniform sampler2D minimapImage;
uniform bool isResized;
uniform ivec2 minimapLocation;
uniform ivec2 minimapPlayerLocation;
uniform float mapAngle;

// Function to get minimap location in screen space
ivec2 getMinimapScreenOffset() {
    if(isResized) {
     return ivec2(minimapLocation.x, sourceDimensions.y - minimapLocation.y - 152);
    } else {
    return ivec2(minimapLocation.x - 4, sourceDimensions.y - minimapLocation.y - 152 + 1);
    }

}

vec4 applyMinimapOverlay(vec4 originalColor);


vec4 applyMinimapOverlay(vec4 originalColor) {
    ivec2 minimapTexSize = textureSize(minimapImage, 0);

    int minimapCircleDiameter = 152;

    ivec2 minimapTopRight = getMinimapScreenOffset();
    ivec2 screenPos = ivec2(gl_FragCoord.xy);

    // Center of the minimap
    vec2 minimapCenter = vec2(minimapTopRight) + vec2(minimapCircleDiameter / 2);

    // Check if the current fragment is within the circular minimap area at the top right of the screen
    if (distance(vec2(screenPos), minimapCenter) <= minimapCircleDiameter / 2) {
        ivec2 relativeScreenPos = screenPos - ivec2(minimapCenter);

        // Calculate the angle and zoom factors
        float angleInRadians = mapAngle / 326.11;
        float cosAngle = cos(angleInRadians);
        float sinAngle = sin(angleInRadians);

        // Apply rotation
        vec2 rotatedPos = vec2(
            relativeScreenPos.x * cosAngle - relativeScreenPos.y * sinAngle,
            relativeScreenPos.x * sinAngle + relativeScreenPos.y * cosAngle
        );

        // Zoom factor
        float zoomFactor = 1.0;
        rotatedPos /= zoomFactor;

        // Calculate the player's location in the minimap texture coordinates
        vec2 playerLoc = minimapPlayerLocation / 16.0 * 16.0;
        vec2 minimapTexCoords = (floor((playerLoc / 32.0 + rotatedPos)) + 0.5) / vec2(minimapTexSize);

        vec4 minimapColor = texture(minimapImage, minimapTexCoords);

        // Change color to red if within tolerance of #EEECF1
        vec3 targetColor = vec3(0.933, 0.925, 0.945); // RGB of #EEECF1 in normalized form
        float tolerance = 0.1;
        if (distance(minimapColor.rgb, targetColor) < tolerance) {
            minimapColor.rgb = vec3(1.0, 0.0, 0.0); // Set to red
        }

        // Applying mask inversion
        ivec2 textureCoordInt = screenPos - minimapTopRight;
        textureCoordInt = ivec2(clamp(textureCoordInt.x, 0, textureSize(minimapMask, 0).x - 1), clamp(textureCoordInt.y, 0, textureSize(minimapMask, 0).y - 1));
        vec4 minimapMaskColor = texelFetch(minimapMask, textureCoordInt, 0);
        originalColor = alphaBlend(originalColor, minimapColor * (1.0 - minimapMaskColor.a));

        return originalColor;
    }

    // Outside the minimap area, return the original color
    return originalColor;
}
#endif


