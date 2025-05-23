#version 330 core

layout(triangles) in;
layout(triangle_strip, max_vertices = 3) out;

in vec3 gPosition[3];
flat in int gHsl[3];

out vec3 gsPosition;
flat out int gsHsl;

void main() {
    for (int i = 0; i < 3; ++i) {
        gsPosition = gPosition[i];
        gsHsl = gHsl[i];
        gl_Position = gl_in[i].gl_Position;
        EmitVertex();
    }
    EndPrimitive();
}