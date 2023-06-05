package de.cofinpro.editor.terminal;

import com.sun.jna.Native;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.green;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.magenta;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;
import static de.cofinpro.editor.terminal.AnsiEscape.red;

@Slf4j
public class Terminal {

    private static final String STATUS_PREFIX = " Jürgen's Terminal Editor: ";
    private final LibC.TermIos normalModeTermIos;
    private final LibC.TermIos rawModeTermIos;
    private final int rows;
    private final int cols;
    private final StringBuilder contents = new StringBuilder();


    public Terminal(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        var termios = logTermIos();
        if (termios.inRawMode()) {
            this.rawModeTermIos = termios;
            this.normalModeTermIos = setNormalModeFlags(termios);
        } else {
            this.normalModeTermIos = termios;
            this.rawModeTermIos = setRawModeFlags(termios);
        }
        logTerminalMode(termios.inRawMode());
    }

    public void run() throws IOException {
        setRawMode(true);
        refresh();
        int key = System.in.read();
        while (key != 'Q') {
            switch (key) {
                case 127 -> backspace();
                case 'N' -> refresh();
                case 'W' -> winInfo();
                default -> print(key);
            }
            key = System.in.read();
        }
        close();
    }

    private void close() {
        clearAll();
        log.info(positionCursorTopLeft());
        setRawMode(false);
    }

    private void winInfo() {
        log.info(positionCursor(rows + 1, 1));
        var winSize = new LibC.WinSize();
        var returnCode = LibC.INSTANCE.ioctl(LibC.STDIN_FD, LibC.TIOCGWINSZ, winSize);
        errorExit(returnCode, "ioctl");
        log.info(green("ioctl: {}"), winSize);
        log.info(positionCursorTopLeft());
    }

    private void print(int ascii) {
        if (ascii > 31 && ascii < 127) {
            printChar((char) ascii);
        } else {
            printAscii(ascii);
        }
    }

    private void printAscii(int ascii) {
        printStatusLine(" symbol (%d)".formatted(ascii));
        jumpToEndOfContents();
    }

    private void printChar(char character) {
        jumpToEndOfContents();
        contents.append(character);
        log.info("{}", character);
    }

    private void refresh() {
        clearAll();
        printStatusLine("");
        log.info(positionCursorTopLeft());
        printContents();
        jumpToEndOfContents();
    }

    private void printContents() {
        for (int i = 0; i <= (contents.length() - 1) / cols ; i++) {
            log.info(positionCursor(i + 1, 1));
            log.info(contents.substring(i * cols, Math.min(i * cols + cols, contents.length())));
        }
    }

    private void jumpToEndOfContents() {
        log.info(positionCursor(contents.length() / cols + 1, contents.length() % cols + 1));
    }

    private void backspace() {
        if (contents.isEmpty()) {
            return;
        }
        contents.deleteCharAt(contents.length() - 1);
        refresh();
    }

    private void printStatusLine(String status) {
        log.info(positionCursor(rows, 1));
        var statusMessage = STATUS_PREFIX + status;
        log.info(inverted(statusMessage + " ".repeat(cols - statusMessage.length())));
    }


    private void clearAll() {
        log.info(erase(ALL));
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
        var returnCode = LibC.INSTANCE.tcsetattr(LibC.STDIN_FD, LibC.TCSAFLUSH,
                enabled ? rawModeTermIos : normalModeTermIos);
        errorExit(returnCode, "tcsetattr");
        logTermIos();
    }

    private LibC.TermIos logTermIos() {
        var termios = new LibC.TermIos();
        var returnCode = LibC.INSTANCE.tcgetattr(LibC.STDIN_FD, termios);
        errorExit(returnCode, "tcgetattr");
        log.info(green("tcgetattr: {}\n"), termios);
        return termios;
    }

    private void logTerminalMode(boolean isRaw) {
        log.info(magenta("Terminal is in {} mode!\n"), isRaw ? "raw" : "normal");
    }

    private void errorExit(int returnCode, String problemCall) {
        if (returnCode != 0) {
            log.error(red("Problem with {}! Return code {}\n"), problemCall, returnCode);
            log.error(red("errno='{}'\n"), LibC.INSTANCE.strerror(Native.getLastError()));
            System.exit(returnCode);
        }
    }
}
