#version 330 core

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vColour;
layout(location = 2) in vec2 vTexCoord;

out vec4 color;
out vec2 texCoord;

uniform mat4 ModelView;
uniform mat4 Projection;

void main()
{
    gl_Position = Projection * ModelView * vPosition;
    texCoord    = vTexCoord;
    
    color.rgb = vColour;
    color.a = 1.0;
}
//with reference to lab5 ColourTex,vert