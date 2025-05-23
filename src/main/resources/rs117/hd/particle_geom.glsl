#version 330 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;
uniform mat4 projectionMatrix;
in vec3 gPosition[3];
flat in int gHsl[3];

out vec3 gsPosition;
flat out int gsHsl;

void main() {
    for (int i = 0; i < 3; ++i) {
        gsPosition = gPosition[i];
        gsHsl = gHsl[i];
        gl_Position = projectionMatrix * vec4(gPosition[i], 1.0);
        EmitVertex();
    }
    EndPrimitive();
}