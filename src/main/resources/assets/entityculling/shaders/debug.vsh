#version 110

attribute vec3 a_Pos;

uniform mat4 u_ModelViewProjectionMatrix;

void main() {
  gl_Position = u_ModelViewProjectionMatrix * vec4(a_Pos, 1.0);
}