#version 460 core

layout(location = 0) in vec3 bboxMin;
layout(location = 1) in vec3 bboxMax;
layout(location = 2) in int objid;

out VertexData {
  vec3 bboxMin;
  vec3 bboxMax;
  flat int objid;
} OUT;

void main() {
  OUT.bboxMin = bboxMin;
  OUT.bboxMax = bboxMax;
  OUT.objid = objid;
}