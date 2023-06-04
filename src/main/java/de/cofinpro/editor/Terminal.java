package de.cofinpro.editor;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Terminal {

    private final LibC.Termios normalModeTermios;
    private final LibC.Termios rawModeTermios;

    public Terminal() {
        var termios = LibC.Termios.of(getTermios());
        var inRawMode = (termios.c_oflag & LibC.OPOST) == 0x0;
        if (inRawMode) {
            this.rawModeTermios = termios;
            this.normalModeTermios = setNormalModeFlags(termios);
        } else {
            this.normalModeTermios = termios;
            this.rawModeTermios = setRawModeFlags(termios);
        }
        log.info("\033[35m Terminal is presently in {} mode!\033[0m", inRawMode ? "raw" : "normal");
    }

    public void keyPressLoop() throws IOException {
        char key = (char) System.in.read();
        while (key != 'Q') {
            if (key == 'R') {
                setRawMode(true);
            }
            if (key == 'O') {
                setRawMode(false);
            }
            log.info("{} ({})", key, (int) key);
            key = (char) System.in.read();
        }
    }

    private LibC.Termios setRawModeFlags(LibC.Termios normal) {
        var result = LibC.Termios.of(normal);
        result.c_lflag &= ~(LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);
        result.c_iflag &= ~(LibC.IXON | LibC.ICRNL);
        result.c_oflag &= ~(LibC.OPOST);
        return result;
    }


    private LibC.Termios setNormalModeFlags(LibC.Termios raw) {
        var result = LibC.Termios.of(raw);
        result.c_lflag |= (LibC.ECHO | LibC.ICANON | LibC.IEXTEN | LibC.ISIG);
        result.c_iflag |= (LibC.IXON | LibC.ICRNL);
        result.c_oflag |= LibC.OPOST;
        return result;
    }

    public void setRawMode(boolean enabled) {
        callTcSetAttribute(enabled);
        log.info("\033[35m Terminal is now in {} mode!\033[0m", enabled ? "raw" : "normal");
    }

    private void callTcSetAttribute(boolean enabled) {
        var returnCode = LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSAFLUSH,
                enabled ? rawModeTermios : normalModeTermios);
        if (returnCode != 0) {
            log.error("Problem with tcsetattr! Return code {}", returnCode);
            System.exit(returnCode);
        }
        getTermios();
    }

    private LibC.Termios getTermios() {
        var termios = new LibC.Termios();
        int returnCode = LibC.INSTANCE.tcgetattr(LibC.SYSTEM_OUT_FD, termios);
        if (returnCode != 0) {
            log.error("Problem with tcgetattr! Return code {}", returnCode);
            System.exit(returnCode);
        }
        log.info("\033[32mtcgetattr: {} \033[0m", termios);
        return termios;
    }
}
