#version 440 core

layout(location = 0) in vec3 a_pos;
layout(location = 1) in vec3 a_offset;
layout(location = 2) in vec3 a_scale;
layout(location = 3) in int a_objid;

uniform mat4 projectionViewMatrix;

flat out int v_objid;

void main() {
  gl_Position = projectionViewMatrix * vec4((a_pos * a_scale) + a_offset, 1);
  v_objid = a_objid;
}