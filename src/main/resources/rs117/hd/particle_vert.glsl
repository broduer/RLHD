#version 330 core

layout(location = 4) in vec3 vPosition;
layout(location = 5) in int vHsl;

out vec3 gPosition;
flat out int gHsl;

void main() {
    gPosition = vPosition;
    gHsl = vHsl;
    gl_Position = vec4(vPosition, 1.0);

}