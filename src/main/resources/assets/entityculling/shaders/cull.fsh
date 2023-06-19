#version 440 core

layout(early_fragment_tests) in;

layout (std430, binding = 1) restrict writeonly buffer visibleBuffer {
  int visibles[];
};

flat in int v_ObjID;

void main() {
  visibles[v_ObjID] = 1;
}