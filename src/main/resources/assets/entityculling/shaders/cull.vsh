#version 440 core

layout(location = 0) in vec3 a_Pos;
layout(location = 1) in vec3 a_Offset;
layout(location = 2) in vec3 a_Size;
layout(location = 3) in int a_ObjID;

uniform mat4 u_ModelViewProjectionMatrix;

flat out int v_ObjID;

void main() {
  gl_Position = u_ModelViewProjectionMatrix * vec4((a_Pos * a_Size) + a_Offset, 1);
  v_ObjID = a_ObjID;
}