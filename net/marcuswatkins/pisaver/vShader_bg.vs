
varying float dest_alpha;
varying vec2 v_texCoord;


attribute vec4 a_position;
attribute vec2 a_texCoord;
attribute vec4 adjustments; //alpha, scale, xshift, yshift


void main()
{

	vec4 b = a_position;

	b.y *= -1; //Flip because screen captures are taken from bottom up
	b.x += adjustments.z; //xshift
	b.y += adjustments.w; //yshift
	gl_Position = b;

   //gl_Position = a_position;
   v_texCoord = a_texCoord;
   	dest_alpha = adjustments.x; //alpha
   
}
