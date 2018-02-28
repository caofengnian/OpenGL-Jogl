#version 330 core

in  vec4 color;
in  vec2 texCoord;

out vec4 fColor;

uniform sampler2D tex;

void main()
{
	fColor = color* texture( tex, texCoord );
}
//with reference to lecture note 17 CMT107 VI P28