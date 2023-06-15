package de.cofinpro.editor.terminal;

import de.cofinpro.editor.model.EditorModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.CURSOR_TO_END;
import static de.cofinpro.editor.terminal.AnsiEscape.back;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.eraseLine;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;

@Slf4j
public class Editor implements RefreshListener {

    private static final String STATUS_PREFIX = " Jürgen's Editor Editor: (L%d C%d) ";
    private final EditorModel model;
    private final Clipping clipping;
    private final Cursor cursor;
    private int rows;
    private int cols;

    public Editor() {
        setWindowSize();
        clipping = new Clipping(0, rows - 1, 0, cols, this);
        model = new EditorModel(clipping);
        cursor = new Cursor(1, 1);
        LibC.INSTANCE.setRawMode();
        refresh();
    }

    public void run() throws IOException {
        refresh();
        int key = System.in.read();
        while (key != 'Q') {
            switch (key) {
                case 127 -> backspace();
                case 13 -> carriageReturn();
                case '\033' -> readEscapeSequence();
                case 'R' -> refresh();
                case 'W' -> resizeWindow();
                default -> print(key);
            }
            key = System.in.read();
        }
        close();
    }

    private void readEscapeSequence() throws IOException {
        int second = System.in.read();
        if (second != '[') {
            print(second);
        } else {
            int third = System.in.read();
            switch (third) {
                case 'A' -> arrowUp();
                case 'B' -> arrowDown();
                case 'C' -> arrowRight();
                case 'D' -> arrowLeft();
                default -> print(third);
            }
            log.info(getStatusLine());
        }
    }

    private void arrowLeft() {
    }

    private void arrowRight() {
    }

    private void arrowDown() {
        cursor.down();
    }

    private void arrowUp() {
        cursor.up();
    }

    private void resizeWindow() {
        setWindowSize();
        refresh();
    }

    private void setWindowSize() {
        var processOutput = new SttyCommand().run();
        var tokens = processOutput.split(" ");
        rows = Integer.parseInt(tokens[0].trim());
        cols = Integer.parseInt(tokens[1].trim());
    }

    private void print(int ascii) {
        if (ascii > 31 && ascii < 187) {
            printChar((char) ascii);
        } else {
            printAscii(ascii);
        }
    }

    private void printAscii(int ascii) {
        log.info(getStatusLine(" symbol (%d)".formatted(ascii)));
    }

    private String placeCursor() {
        var row = cursor.line - clipping.top;
        var column = cursor.column - clipping.left;
        return positionCursor(row, column);
    }

    private void printChar(char character) {
        model.insert(character, cursor);
        if (cursor.forward().column == clipping.right) {
            clipping.scrollRight();
        } else {
            refreshLine(false);
        }
    }

    private void refreshLine(boolean back) {
        var index = back ? clipping.getDisplayCol(cursor.column) : clipping.getDisplayCol(cursor.column) - 2;
        var clippedLine = model.getClippedLine(cursor.line);
        var printText = index <= clippedLine.length() ? clippedLine.substring(index) : back() + " ";
        log.info(eraseLine(CURSOR_TO_END) + printText + getStatusLine());
    }

    private void carriageReturn() {
        model.insertLine(cursor.carriageReturn().line);
        clipping.left();
    }

    public void refresh() {
        log.info(erase(ALL) + getContents() + getStatusLine());
    }

    private String getContents() {
        var sb = new StringBuilder();
        var contents = model.getClippingContent();
        for (int i = 0; i < contents.size(); i++) {
            sb.append(positionCursor(i + 1, 1))
                    .append(contents.get(i));
        }
        return sb.toString();
    }

    private void backspace() {
        if (model.getLines().isEmpty()) {
            return;
        }
        model.deleteCharAt(cursor.back());
        if (cursor.column >= clipping.right) {
            clipping.centerHorizontal(cursor.column);
        } else if (cursor.column == clipping.left + 1) {
            clipping.scrollLeft();
        } else {
            refreshLine(true);
        }
    }

    private String getStatusLine() {
        return getStatusLine("");
    }

    private String getStatusLine(String status) {
        var statusMessage = STATUS_PREFIX.formatted(cursor.line, cursor.column) + status;
        return positionCursor(rows, 1)
               + inverted(statusMessage + " ".repeat(cols - statusMessage.length()))
               + placeCursor();
    }

    private void close() {
        log.info(erase(ALL) + positionCursorTopLeft());
        LibC.INSTANCE.setNormalMode();
    }

    @Data
    @AllArgsConstructor
    public
    class Cursor {
        int line;
        int column;

        Cursor carriageReturn() {
            column = 1;
            line++;
            return this;
        }

        Cursor forward() {
            column++;
            return this;
        }

        Cursor up() {
            if (line == 1) {
                return this;
            }
            line--;
            return verticalMovedCursor();
        }

        Cursor down() {
            if (line == model.lineCount()) {
                return this;
            }
            line++;
            return verticalMovedCursor();
        }

        private Cursor verticalMovedCursor() {
            var colsinLine = model.getColsInLine(line);
            if (column > colsinLine) {
                column = colsinLine + 1;
            }
            return this;
        }

        Cursor back() {
            if (column > 1) {
                column--;
                return this;
            }
            if (line == 1) {
                return this;
            }
            line--;
            column = model.getColsInLine(line) + 1;
            return this;
        }
    }
}
