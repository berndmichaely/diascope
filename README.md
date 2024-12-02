# [Diascope](https://github.com/berndmichaely/diascope)

![version](doc/shields/version.svg "version")

## Getting the application

### Building the dependencies

First, you need once to locally build the `-base` and `-fx` libraries from the `lib-filesystem-view` repository and publish them to the local maven cache, see:

[github.com/berndmichaely/lib-filesystem-view](https://github.com/berndmichaely/lib-filesystem-view)

### Building the application

Have a recent JDK installed, get the sources and run from the root directory:

`> ./gradlew run`

to run the application immediately or

`> ./gradlew installDist`

to build the application into the `./build/install/Diascope` directory.
