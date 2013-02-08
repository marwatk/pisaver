

attribute float my_alpha;
attribute vec4 a_position;
attribute vec2 a_texCoord;
varying vec2 v_texCoord;

attribute float rad_angle;
attribute float scale;
attribute float x_shift;
attribute float y_shift;
varying float dest_alpha;

void main()
{
	vec4 b = a_position;

	b.x = a_position.x*cos(rad_angle) - a_position.y*sin(rad_angle);
	b.y = a_position.y*cos(rad_angle) + a_position.x*sin(rad_angle);

	b.x *= scale;
	b.y *= scale;

	b.x += x_shift;
	b.y += y_shift;

	gl_Position = b;
	v_texCoord = a_texCoord;
	dest_alpha = my_alpha;
}
