precision mediump float;
varying float dest_alpha;
varying vec2 v_texCoord;                            
uniform sampler2D s_texture;

void main()                                         
{
	vec4 color = texture2D( s_texture, v_texCoord );
	
	color.a *= dest_alpha;
  //gl_FragColor = vec4( 1, 1, 1, dest_alpha)*texture2D( s_texture, v_texCoord );
  gl_FragColor = color;
}