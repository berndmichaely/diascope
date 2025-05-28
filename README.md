# [Diascope](https://github.com/berndmichaely/diascope)

![version](doc/shields/version.svg "version")

Diascope is a Java/JavaFX based desktop application. It is basically an image viewer and contains a multi layer image mode for enhanced multi image comparison (and hopefully more in the future…).

## Building the application

Have a recent JDK installed, get the sources and run from the root directory:

`> ./gradlew run`

to run the application immediately or

`> ./gradlew installDist`

to build the application into the `./build/install/Diascope` directory (it is tested with Eclipse Adoptium JDK 24).

### Trial hints

Select the test image directory `./doc/test-images/png` and add alternately a layer (using the **+** button in the toolbar) and select another test image (a section must be selected to change the image). Then try the functionality available in the toolbar:

![Screenshot 2](doc/screenshots/Screenshot_01.png "Screenshot 1")

Try to compare e.g. different post processed versions of a RAW image, exposure bracketing series…

![Screenshot 2](doc/screenshots/Screenshot_02.png "Screenshot 2")

### Full Screen Mode

In single image layer mode, you can use a double click on the image area to enter full screen mode. In multi layer mode, you can use Shift+DoubleClick. (In both cases, you can also use the menu, toolbar button or image area context menu.)

Configuration like showing/hiding toolbar/thumbnail/dividers is kept and remembered for window and full screen modes separately.

### Multi Layer Mode

  * Use the **+** button to add layers.
  * Single-Click a layer to select it. Ctrl-Single-Click to select several layers.
  * Use the **-** button to remove the selected layers.
  * Image navigation sets the image in a single selected layer only.
  * Use context menu functions to select all/no layers or invert selection.

#### Dividers

  * To move the dividers, drag the split center with the mouse.
  * Drag a divider with the mouse to rotate the dividers.
  * Ctrl-Drag a divider with the mouse to rotate a single divider only.
  * Shift-Drag a divider with the mouse to fold dividers.
  * Shift+Ctrl-Drag a divider with the mouse: reserved – currently resets the dividers to the initial state of the current mouse drag cycle.
