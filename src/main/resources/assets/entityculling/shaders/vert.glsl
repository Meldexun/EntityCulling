#version 440 core

layout(location = 0) in vec3 v_pos;
layout(location = 1) in vec3 v_offset;
layout(location = 2) in vec3 v_scale;
layout(location = 3) in int v_objid;

uniform mat4 projectionViewMatrix;

flat out int f_objid;

void main() {
  gl_Position = projectionViewMatrix * vec4((v_pos * v_scale) + v_offset, 1);
  f_objid = v_objid;
}