pisaver
=======

Slideshow collage screensaver for the raspberry pi

I had intended to just use this for me so the code isn't great, but it could be a good example for others so I figured I'd share. It simply displays pictures from folders that match specific tag and rating criteria. The images are displayed using java and OpenGL in an animated grid on the screen. (Size of the grid depends on screen aspect ratio). I use it on a low power led-tv when we're not watching anything. (My previous setup was an old computer and a slew of monitors running Picasa's screen saver, this uses about a tenth of the power)

See the install instructions here:

https://github.com/marwatk/pisaver/blob/master/INSTALL.md

Depending on the size of your images, initial loading could take up to 60 seconds per image (resizing is slow on the pi), but after each image has been loaded cache is used and it starts going much faster (new images appear every 8 seconds or so).
