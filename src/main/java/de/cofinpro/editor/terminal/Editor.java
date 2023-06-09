package de.cofinpro.editor.terminal;

import de.cofinpro.editor.model.EditorModel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.eraseLine;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;

@Slf4j
public class Editor implements Refreshable {

    private static final int CTRL_Q = 17;
    private static final int CTRL_W = 23;
    private static final int RETURN = 13;
    private static final int BACKSPACE = 127;
    private static final String STATUS_PREFIX = " Jürgen's Editor: (L%d C%d) ";
    private final EditorModel model;
    private final Clipping clipping;
    private final Cursor cursor;
    private int rows;
    private int cols;

    public Editor() {
        setWindowSize();
        model = new EditorModel();
        clipping = new Clipping(rows, cols, this);
        cursor = new Cursor(model, 1, 1);
        LibC.INSTANCE.setRawMode();
        refresh();
    }

    @Override
    public void refresh() {
        log.info(erase(ALL) + clippingContents() + updateDisplayAndStatus());
    }

    @Override
    public void refreshLine() {
        var clippedLine = model.getClippedLine(cursor.line, clipping);
        log.info(positionStartOfLine() + eraseLine(ALL) + clippedLine + updateDisplayAndStatus());
    }

    public void run() throws IOException {
        refresh();
        int key = System.in.read();
        while (key != CTRL_Q) {
            switch (key) {
                case BACKSPACE -> backspace();
                case RETURN -> carriageReturn();
                case '\033' -> readEscapeSequence();
                case CTRL_W -> resizeWindow(); // Ctrl-W
                default -> print(key);
            }
            key = System.in.read();
        }
        close();
    }

    private void print(int ascii) {
        if (ascii > 31 && ascii < 187) {
            printChar((char) ascii);
        } else {
            printAscii(ascii);
        }
    }

    private void printAscii(int ascii) {
        log.info(updateDisplayWithStatus(" symbol (%d)".formatted(ascii)));
    }

    private void printChar(char character) {
        model.insert(character, cursor);
        clipping.setPosition(cursor.forward(true));
    }

    private void carriageReturn() {
        var column = cursor.column;
        model.insertLine(cursor.carriageReturn().line, column);
        clipping.setPosition(cursor);
        refresh();
    }

    private void backspace() {
        if (cursor.isAtStartOfBuffer()) {
            return;
        }
        model.deleteCharAt(cursor.back());
        clipping.setPosition(cursor);
        refresh();
    }

    private void readEscapeSequence() throws IOException {
        int second = System.in.read();
        if (second != '[') {
            print(second);
        } else {
            int third = System.in.read();
            switch (third) {
                case 'A' -> cursor.up();
                case 'B' -> cursor.down();
                case 'C' -> cursor.forward();
                case 'D' -> cursor.back();
                default -> print(third);
            }
            clipping.setPosition(cursor);
            log.info(updateDisplayAndStatus());
        }
    }

    private String clippingContents() {
        var sb = new StringBuilder();
        var contents = model.getClippingContent(clipping);
        for (int i = 0; i < contents.size(); i++) {
            sb.append(positionCursor(i + 1, 1))
                    .append(contents.get(i));
        }
        return sb.toString();
    }

    private String updateDisplayAndStatus() {
        return updateDisplayWithStatus("");
    }

    private String updateDisplayWithStatus(String status) {
        var statusMessage = STATUS_PREFIX.formatted(cursor.line, cursor.column) + status;
        return positionCursor(rows, 1)
               + inverted(statusMessage + " ".repeat(cols - statusMessage.length()))
               + placeCursor();
    }

    private String placeCursor() {
        var row = cursor.line - clipping.getTop();
        var column = cursor.column - clipping.getLeft();
        return positionCursor(row, column);
    }

    private String positionStartOfLine() {
        return positionCursor(cursor.line - clipping.getTop(), 1);
    }

    private void resizeWindow() {
        setWindowSize();
        clipping.resize(rows, cols, cursor);
        refresh();
    }

    private void setWindowSize() {
        var processOutput = new SttyCommand().run();
        var tokens = processOutput.split(" ");
        rows = Integer.parseInt(tokens[0].trim());
        cols = Integer.parseInt(tokens[1].trim());
    }

    private void close() {
        log.info(erase(ALL) + positionCursorTopLeft());
        LibC.INSTANCE.setNormalMode();
    }
}
