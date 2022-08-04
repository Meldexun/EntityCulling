#version 440 core

layout(early_fragment_tests) in;

layout (std430, binding = 1) restrict buffer visibleBuffer {
  int visibles[];
};

flat in int v_ObjID;

uniform int u_Frame;

out vec4 f_Color;

void main() {
  if (visibles[v_ObjID] != u_Frame) {
    visibles[v_ObjID] = u_Frame;
  }

  f_Color = vec4(1, 1, 1, 0.5);
}