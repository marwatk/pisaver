This document assumes you've got a fresh pi wheezy/jessie/stretch install and your network is 
up and running installing everything to `/home/pi/pisaver`

Set up app dir:
```
    mkdir /home/pi/pisaver
    cd /home/pi/pisaver
```

Remove conflicting gl driver
See http://forum.jogamp.org/Jogl-with-raspberry-pi3-td4037813.html
And https://github.com/anholt/mesa/issues/24
```
    sudo aptitude remove libgles2-mesa
```

Install raspberry pi java and 7zip
```
    sudo apt-get update
    sudo apt-get install oracle-java7-jdk p7zip-full
```

Download pisaver:
```
    wget https://github.com/marwatk/pisaver/releases/download/2.0/pisaver-2.0.2.jar
```

Make a configuration file (`pisaver.prop`):
```
#If present minRating specifies the minimum rating an image must have to be displayed (inclusive)
#Unrated pictures are marked as 0.0
minRating = 4.0

#If present maxRating specifies the maximum rating an image can have to be displayed
#maxRating = 5.0

#If present, only images tagged with these tags will be included for display (separated by semicolons)
includeTags =

#If present, any images containing these tags will never be displayed. Exclusion wins over inclusion. (separated by semicolons)
excludeTags = no show;noshow

#If present these dimensions will override detected settings (currently detection only works when X is running)
width = 1920
height = 1080
```
Mount your source folders (this step may vary, my folders are on a windows share)
```
    sudo apt-get install cifs-utils
    sudo mkdir /mnt/pictures
    sudo mount -o username=<username>,password=<password> //<computer>/<share> /mnt/pictures
```

Run the screensaver (you can add as many pictures folders as you want to the end of the command):
```
java -jar pisaver-2.0.2.jar /mnt/pictures
```