# Editor project as playground repo

Test some stuff inspired by Editor Series of Marco Codes on You Tube.

## Technology / External Libraries

- Java 20
- JNA, to call libc methods of MacOs Ventana 13.3
- Log4j2 with custom programmatic configuration
- Gradle 8.3

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
project directory. You can find the executable bash-script then in:
> $project_dir/build/install/editor/bin/editor

You need to `cd` there now from a MacOS terminal and just run above script. It is _not_ possible to run the application from
within IDE, since the Run-terminals here are emulated and the JNA-calls won't work.

## Usage

Currently implemented control keys:

> `CTRL-Q`: **Quit** the editor.
>
> `BACKSPACE`: delete character in front of cursor (no surprise here :-)
> 
> `Arrow-Keys`: navigate through the text buffer - viewport scrolls to center cursor position as soon as the boundary is
touched or when jumping out of the viewport (similar as and inspired by the behaviour of emacs)
> 
> `Ctrl-A`: Jump to **Begin of Line**.
>
> `Ctrl-E`: Jump to **End of Line** (behind last character).
>
> `Esc-<` (or `M-<` Emacs-style): Jump to **Begin of Buffer**.
>
> `Esc->` (or `M->`): Jump to **End of Buffer**.
>
> `Ctrl-V`: **Page-Down** - scroll down one page (# rows in display).
>
> `Esc-V` (or `M-V`): **Page-Up** - scroll up one page (# rows in display).
>
> `Ctrl-F`: Interactive and **incremental Search**. Entered search text can incrementally searched forward (`n` next)
or backwards (`p` previous)
>
> `Ctrl-R`: Interactive **Search backwards**. Entering the incremental search above.
> 
> `Ctrl-S`: **Save buffer** to file. Filename is entered in the statusbar - relative to working directory.
>
> `Ctrl-L`: **Load buffer** from file. Filename is entered in the statusbar - relative to working directory.
>
> `Ctrl-W`: After a manual terminal **resize** adjust the running editor to the new dimensions (Internally it runs an stty-
command and uses its output to set new clipping dimensions).
>

## Project status

Project is ongoing.

[//]: # (Project was completed on 14.05.23.)

## Progress

04.06.23 Project started. Setup of build and repo with gradle on Kotlin basis. Configuration of log4j with
custom ConfigurationFactory.

05.06.23 Some refactoring. Doing a keyPress-loop in Terminal to toggle between terminal's raw and normal mode.

01.07.23 Core editor functionality implemented based on an OOP-design with an Editor (controller-type) class, that
instantiates and uses EditorModel, Cursor and Clipping objects. The editor scrolls its viewport vertically as well
as horizontally (unlike emacs or vi, who in base mode do not scroll horizontally).

27.08.23 Besides some minor refactoring, load and save functionality to and from file added. Filename is entered in
the status bar implemented in new inner class Editor.FileHandler using new methods in the EditorModel and Cursor.

28.08.23 Page-Up and -down implemented using the Emacs key shortcuts. File-IO exception handling added. Jump to
End and Begin of Line added - again with Emacs shortcuts.

02.09.23 Jump to Begin and End of Buffer added (using Emacs Shortcuts M-<, M->) and incremental search forwards and backwards
on Ctrl-F and Ctrl-R.
