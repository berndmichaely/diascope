# Diascope – General Documentation

## Gradle User Properties

Command line arguments to be passed to the application through Gradle can be declared in the file:

`${HOME}/.gradle/gradle.properties`

Example:

    !
    ! Diascope command line arguments:
    !
    diascope.arg.0=--development
    diascope.arg.1=running from within gradle
    !
    ! Custom JDK locations:
    !
    #org.gradle.java.installations.fromEnv=JDK20,JDK21
    #org.gradle.java.installations.paths=/path/to/jdk22,/path/to/jdk23
    !
    ! Custom semantic version build identifier:
    !
    #diascope.build.identifier=my-linux-distro

## Storing Preferences

Preferences like main window size and position and the like are stored using the [Preferences](https://docs.oracle.com/en/java/javase/21/docs/api/java.prefs/java/util/prefs/Preferences.html) API of the JDK [`java.prefs` module](https://docs.oracle.com/en/java/javase/21/docs/api/java.prefs/module-summary.html).

However, the API is not used directly, but via the `PreferencesUtil` class in the `de.bernd_michaely.diascope.app` package. This utility allows to use distinct preferences root nodes for different launch types (normal, development, unit-test).

This allows a stable version, a development version and unit tests to coexist on the same host without disturbing each other.

So it is **important** to **always** use this utility class instead of the preferences API directly!

Keys for preferences must be defined in the `enum PreferencesKeys`.
