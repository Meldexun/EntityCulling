#version 440 core

layout(early_fragment_tests) in;

layout (std430, binding = 1) restrict buffer visibleBuffer {
  int visibles[];
};

flat in int v_objid;

uniform int frame;

out vec4 f_color;

void main() {
  if (visibles[v_objid] != frame) {
    visibles[v_objid] = frame;
  }

  f_color = vec4(1, 1, 1, 0.5);
}