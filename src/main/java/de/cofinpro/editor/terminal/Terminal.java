package de.cofinpro.editor.terminal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;

import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.green;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;

@Slf4j
public class Terminal {

    private static final String STATUS_PREFIX = " Jürgen's Terminal Editor: ";
    private final LibC.TermIos normalModeTermIos;
    private final LibC.TermIos rawModeTermIos;
    private int rows;
    private int cols;
    private final StringBuilder contents = new StringBuilder();


    public Terminal(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        var termios = LibC.TermIos.getTermIos();
        if (termios.inRawMode()) {
            this.rawModeTermIos = termios;
            this.normalModeTermIos = termios.withNormalModeFlags();
        } else {
            this.normalModeTermIos = termios;
            this.rawModeTermIos = termios.withRawModeFlags();
        }
    }

    public void run() throws IOException {
        LibC.INSTANCE.setMode(rawModeTermIos);
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

    private void winInfo() throws IOException {
        var process = new ProcessBuilder("stty", "-f", "/dev/tty", "size").start();
        var streamReader = new InputStreamReader(process.getInputStream());
        var builder = new StringBuilder();
        var i = streamReader.read();
        while (i != -1) {
            builder.append((char) i);
            i = streamReader.read();
        }
        var tokens = builder.toString().split(" ");
        rows = Integer.parseInt(tokens[0].trim());
        cols = Integer.parseInt(tokens[1].trim());
        log.info(positionCursor(rows + 1, 1));
        log.info(green("winsize: rows {}, columns {}"), rows, cols);
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

    private void close() {
        clearAll();
        log.info(positionCursorTopLeft());
        LibC.INSTANCE.setMode(normalModeTermIos);
    }
}
