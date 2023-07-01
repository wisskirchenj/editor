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

    void forward() {
        forward(false);
    }

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

    Cursor forward(boolean stayInLine) {
        if (stayInLine || column <= model.getColsInLine(line)) {
            column++;
            return this;
        }
        if (line == model.lineCount()) {
            return this;
        }
        line++;
        column = 1;
        return this;
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

    private void verticalMovedCursor() {
        var colsInLine = model.getColsInLine(line);
        if (column > colsInLine) { //set cursor after last char
            column = colsInLine + 1;
        }
    }

    boolean isAtStartOfBuffer() {
        return line == 1 && column == 1;
    }
}
