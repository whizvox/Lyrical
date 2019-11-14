# Lyrical

## Introduction

This is a relatively-small easy-to-use karaoke program written entirely in Java.

## How to build

The following must be installed:

* Java 8
* [Gradle 4.x](https://gradle.org/)

Run the command `./gradlew dist`. This will generate a fat jar in `/desktop/build/libs/Lyrical-x.jar`.

## How to run

The following must be installed:

* Java 8+ (tested with OpenJDK 8 and 11)
* [ffmpeg](https://ffmpeg.org) (if you want to edit songs)

Run the command `./gradlew :desktop:run`. This will create a directory in `/desktop/.rundir` which acts as the working
directory.

You can also run a downloaded binary or the generated jar from the [How to build](#how-to-build) section via the
command `java -jar Lyrical.x-jar`.

## Basic instructions

Everything is keyboard-controlled. Use the arrow keys to navigate between options. Press `F1` in the song selection
screen to edit the currently-selected song. Press `H` in the editor to open the help screen. There isn't an official
way to distribute songs as of yet.