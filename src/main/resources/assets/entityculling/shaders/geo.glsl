#version 460 core

uniform mat4 projectionViewMatrix;

layout(points) in;
layout(triangle_strip, max_vertices = 14) out;

in VertexData {
  vec3 bboxMin;
  vec3 bboxMax;
  flat int objid;
} IN[1];

flat out int objid;

void main() {
  vec3 bboxSize = IN[0].bboxMax - IN[0].bboxMin;

  vec4 vx = vec4(bboxSize.x, 0, 0, 0);
  vec4 vy = vec4(0, bboxSize.y, 0, 0);
  vec4 vz = vec4(0, 0, bboxSize.z, 0);

  vec4 v000 = vec4(IN[0].bboxMin, 1);
  vec4 v001 = v000 + vz;
  vec4 v010 = v000 + vy;
  vec4 v011 = v010 + vz;
  vec4 v100 = v000 + vx;
  vec4 v101 = v100 + vz;
  vec4 v110 = v100 + vy;
  vec4 v111 = v110 + vz;

  v000 = projectionViewMatrix * v000;
  v001 = projectionViewMatrix * v001;
  v010 = projectionViewMatrix * v010;
  v011 = projectionViewMatrix * v011;
  v100 = projectionViewMatrix * v100;
  v101 = projectionViewMatrix * v101;
  v110 = projectionViewMatrix * v110;
  v111 = projectionViewMatrix * v111;

  objid = IN[0].objid;
  gl_Position = v000;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v100;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v001;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v101;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v111;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v100;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v110;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v000;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v010;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v001;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v011;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v111;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v010;
  EmitVertex();

  objid = IN[0].objid;
  gl_Position = v110;
  EmitVertex();


/*
  objid = 0;
  gl_Position = vec4(-0.5, -0.5, 0, 1);
  EmitVertex();

  gl_Position = vec4(0.5, -0.4, 0, 1);
  EmitVertex();

  gl_Position = vec4(0.2, 0.5, 0, 1);
  EmitVertex();
*/
}