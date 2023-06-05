# Editor project as playground repo

Test some stuff inspired by Editor Series of Marco Codes on You Tube.

## Technology / External Libraries

- Java 20
- JNA, to call libc methods of MacOs Ventana 13.3
- Log4j2 with custom programmatic configuration
- Tests with Junit-Jupiter and Mockito
- Gradle 8.1.1

## Program description

Terminal editor app using JNA (to use OS-function for switching teminal modes etc.) and ANSI-sequences. 
Should give some text-editor as vi in "very light" .-)

## Instructions

**Note:** The application is presently *restricted to work on MacOS*. Without adaptations of the libc-parameters
it most probably won't work on Linux or Windows WSL. But for the latter you can use
[Marco Behler's GitHub repository](https://github.com/marcobehlerjetbrains/text-editor).

So for Mac-users, after cloning use the gradle task
`gradlew installDist`
to install a runnable project. This will install your project (at least for IntelliJ Idea) in a subfolder of the
project directory. You can find the executable bash-script then hier:
> $project_dir/build/install/editor/bin/editor

You need to `cd` there now from a MacOS terminal and just run above script. It is _not_ possible to run the application from
within IDE, since the Run-terminals here are emulated and the JNA-calls won't work.

## Project status

Project is ongoing.

[//]: # (Project was completed on 14.05.23.)

## Progress

04.06.23 Project started. Setup of build and repo with gradle on Kotlin basis. Configuration of log4j with
custom ConfigurationFactory.

05.06.23 Some refactoring. Doing a keyPress-loop in Terminal to toggle between terminal's raw and normal mode.
