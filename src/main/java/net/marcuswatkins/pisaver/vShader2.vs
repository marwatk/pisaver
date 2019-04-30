
varying float dest_alpha;
varying vec2 v_texCoord;


attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute float rad_angle;
attribute vec4 adjustments; //alpha, scale, xshift, yshift
attribute float scale_y;


void main()
{

	vec4 b = a_position;

	b.x = a_position.x*cos(rad_angle) - a_position.y*sin(rad_angle);
	b.y = a_position.y*cos(rad_angle) + a_position.x*sin(rad_angle);

	b.x *= adjustments.y; //scale x
	b.y *= scale_y; //scale y

	b.x += adjustments.z; //xshift
	b.y += adjustments.w; //yshift

	gl_Position = b;

   //gl_Position = a_position;
   v_texCoord = a_texCoord;
   	dest_alpha = adjustments.x; //alpha
   
}
