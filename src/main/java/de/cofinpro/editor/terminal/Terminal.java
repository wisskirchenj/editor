package de.cofinpro.editor.terminal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;

@Slf4j
public class Terminal {

    private static final String STATUS_PREFIX = " Jürgen's Terminal Editor: ";
    private final StringBuilder contents = new StringBuilder();
    private int rows;
    private int cols;

    public Terminal() {
        setWindowSize();
        LibC.INSTANCE.setRawMode();
    }

    public void run() throws IOException {
        refresh();
        int key = System.in.read();
        while (key != 'Q') {
            switch (key) {
                case 127 -> backspace();
                case 'N' -> refresh();
                case 'W' -> setWindowSize();
                default -> print(key);
            }
            key = System.in.read();
        }
        close();
    }

    private void setWindowSize() {
        var processOutput = new SttyCommand().run();
        var tokens = processOutput.split(" ");
        rows = Integer.parseInt(tokens[0].trim());
        cols = Integer.parseInt(tokens[1].trim());
    }

    private void print(int ascii) {
        if (ascii > 31 && ascii < 127) {
            printChar((char) ascii);
        } else {
            printAscii(ascii);
        }
    }

    private void printAscii(int ascii) {
        log.info(printStatusLine(" symbol (%d)".formatted(ascii)) + jumpToEndOfContents());
    }

    private void printChar(char character) {
        log.info(jumpToEndOfContents());
        log.info("{}", character);
        contents.append(character);
    }

    private void refresh() {
        log.info(erase(ALL) + printStatusLine("")
                 + positionCursorTopLeft() + printContents()
                 + jumpToEndOfContents());
    }

    private String printContents() {
        var sb = new StringBuilder();
        for (int i = 0; i <= (contents.length() - 1) / cols; i++) {
            sb.append(positionCursor(i + 1, 1));
            sb.append(contents.substring(i * cols, Math.min(i * cols + cols, contents.length())));
        }
        return sb.toString();
    }

    private String jumpToEndOfContents() {
        return positionCursor(contents.length() / cols + 1, contents.length() % cols + 1);
    }

    private void backspace() {
        if (contents.isEmpty()) {
            return;
        }
        contents.deleteCharAt(contents.length() - 1);
        refresh();
    }

    private String printStatusLine(String status) {
        var statusMessage = STATUS_PREFIX + status;
        return positionCursor(rows, 1)
               + inverted(statusMessage + " ".repeat(cols - statusMessage.length()));
    }

    private void close() {
        log.info(erase(ALL) + positionCursorTopLeft());
        LibC.INSTANCE.setNormalMode();
    }
}