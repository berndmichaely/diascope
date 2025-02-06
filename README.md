# [Diascope](https://github.com/berndmichaely/diascope)

![version](doc/shields/version.svg "version")

Diascope is basically a multi layer image viewer for enhanced multi image comparison (and hopefully more in the future…).

## Building the application

Have a recent JDK installed, get the sources and run from the root directory:

`> ./gradlew run`

to run the application immediately or

`> ./gradlew installDist`

to build the application into the `./build/install/Diascope` directory.

### Trial hints

Select the test image directory `./doc/test-images/png` and add alternately a layer (using the **+** button in the toolbar) and select another test image (a section must be selected to change the image). Then try the functionality available in the toolbar:

![Screenshot 2](doc/screenshots/Screenshot_01.png "Screenshot 1")

Try to compare e.g. different post processed versions of a RAW image, exposure bracketing series…

![Screenshot 2](doc/screenshots/Screenshot_02.png "Screenshot 2")

In single image layer mode, you can use a double click to enter fullscreen mode. In multi layer mode, use Shift+DoubleClick.
