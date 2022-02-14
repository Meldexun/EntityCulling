#version 460 core

layout(early_fragment_tests) in;

uniform int frame;

layout (std430, binding = 1) buffer visibleBuffer {
  int visibles[];
};

out vec4 FragColor;

flat in int objid;

void main() {
  visibles[objid] = frame;

  FragColor = vec4(1, 1, 1, 0.5);
}