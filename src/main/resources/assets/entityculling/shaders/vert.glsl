#version 130

in vec3 a_pos;
in vec3 a_offset;
in vec3 a_scale;
in int a_objid;

flat out int v_objid;

void main() {
  gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * vec4((a_pos * a_scale) + a_offset, 1);
  v_objid = a_objid;
}