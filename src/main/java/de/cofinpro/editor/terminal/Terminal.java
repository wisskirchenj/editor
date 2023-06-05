package de.cofinpro.editor.terminal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Terminal {

    private final LibC.TermIos normalModeTermIos;
    private final LibC.TermIos rawModeTermIos;

    public Terminal() {
        var termios = LibC.TermIos.of(callAndLogTcgetattr());
        if (termios.inRawMode()) {
            this.rawModeTermIos = termios;
            this.normalModeTermIos = setNormalModeFlags(termios);
        } else {
            this.normalModeTermIos = termios;
            this.rawModeTermIos = setRawModeFlags(termios);
        }
        logTerminalMode(termios.inRawMode());
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

    void setRawMode(boolean enabled) {
        callTcsetattr(enabled);
        logTerminalMode(enabled);
    }

    private LibC.TermIos setRawModeFlags(LibC.TermIos normal) {
        var result = LibC.TermIos.of(normal);
        result.cLflag &= ~LibC.RAW_TOGGLE_LFLAGS;
        result.cIflag &= ~LibC.RAW_TOGGLE_IFLAGS;
        result.cOflag &= ~LibC.RAW_TOGGLE_OFLAGS;
        return result;
    }

    private LibC.TermIos setNormalModeFlags(LibC.TermIos raw) {
        var result = LibC.TermIos.of(raw);
        result.cLflag |= LibC.RAW_TOGGLE_LFLAGS;
        result.cIflag |= LibC.RAW_TOGGLE_IFLAGS;
        result.cOflag |= LibC.RAW_TOGGLE_OFLAGS;
        return result;
    }

    private void callTcsetattr(boolean enabled) {
        var returnCode = LibC.INSTANCE.tcsetattr(LibC.SYSTEM_OUT_FD, LibC.TCSAFLUSH,
                enabled ? rawModeTermIos : normalModeTermIos);
        errorExit(returnCode, "tcsetattr");
        callAndLogTcgetattr();
    }

    private LibC.TermIos callAndLogTcgetattr() {
        var termios = new LibC.TermIos();
        var returnCode = LibC.INSTANCE.tcgetattr(LibC.SYSTEM_OUT_FD, termios);
        errorExit(returnCode, "tcgetattr");
        log.info("\033[32mtcgetattr: {} \033[0m", termios);
        return termios;
    }

    private void logTerminalMode(boolean isRaw) {
        log.info("\033[35m Terminal is in {} mode!\033[0m", isRaw ? "raw" : "normal");
    }

    private static void errorExit(int returnCode, String problemCall) {
        if (returnCode != 0) {
            log.error("Problem with {}! Return code {}", problemCall, returnCode);
            System.exit(returnCode);
        }
    }
}
