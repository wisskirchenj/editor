package de.cofinpro.editor.terminal;

import de.cofinpro.editor.model.EditorModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Cursor {
    private final EditorModel model;
    int line;
    int column;

    void up() {
        if (line == 1) {
            return;
        }
        line--;
        verticalMovedCursor();
    }

    void down() {
        if (line == model.lineCount()) {
            return;
        }
        line++;
        verticalMovedCursor();
    }

    public void jumpToLine(int targetLine) {
        line = targetLine;
        verticalMovedCursor();
    }

    private void verticalMovedCursor() {
        var colsInLine = model.getColsInLine(line);
        if (column > colsInLine) { //set cursor after last char
            column = colsInLine + 1;
        }
    }

    Cursor forward() {
        if (column <= model.getColsInLine(line)) {
            column++;
            return this;
        }
        if (line == model.lineCount()) {
            return this;
        }
        return carriageReturn();
    }

    Cursor carriageReturn() {
        column = 1;
        line++;
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

    boolean isAtStartOfBuffer() {
        return line == 1 && column == 1;
    }

    Cursor topLeft() {
        line = 1;
        column = 1;
        return this;
    }
}
