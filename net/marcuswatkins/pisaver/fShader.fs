precision mediump float;
varying float dest_alpha;
varying vec2 v_texCoord;                            
uniform sampler2D s_texture;

void main()                                         
{
	vec4 alteredColor = texture2D( s_texture, v_texCoord );
	vec4 color = alteredColor.yzwx; //This exploits that we're passing in the result of getRGB from a BufferedImage (ARGB) instead of a properly arranged byte array to glTexImage2D (OMG the pi is SLOW)
	// vec4 color = alteredColor;
	
	color.a *= dest_alpha;
  //gl_FragColor = vec4( 1, 1, 1, dest_alpha)*texture2D( s_texture, v_texCoord );
  gl_FragColor = color;
}