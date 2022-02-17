#version 440 core

layout(early_fragment_tests) in;

layout (std430, binding = 1) buffer visibleBuffer {
  int visibles[];
};

flat in int f_objid;

uniform int frame;

out vec4 FragColor;

void main() {
  visibles[f_objid] = frame;

  FragColor = vec4(1, 1, 1, 0.5);
}