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

    Cursor jumpToLine(int targetLine) {
        line = targetLine;
        verticalMovedCursor();
        return this;
    }

    private void verticalMovedCursor() {
        var colsInLine = model.getColsInLine(line);
        if (column > colsInLine) { //set cursor after last char
            lineEnd();
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
        line++;
        return lineBegin();
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
        return lineEnd();
    }

    boolean isAtStartOfBuffer() {
        return line == 1 && column == 1;
    }

    Cursor topLeft() {
        line = 1;
        return lineBegin();
    }

    Cursor lineBegin() {
        column = 1;
        return this;
    }

    Cursor lineEnd() {
        column = model.getColsInLine(line) + 1;
        return this;
    }

    Cursor setPosition(Position position) {
        column = position.column();
        return jumpToLine(position.line());
    }

    Position getPosition() {
        return new Position(line, column);
    }

    public Cursor jumpBeginOfBuffer() {
        line = 1;
        column = 1;
        return this;
    }

    public Cursor jumpEndOfBuffer() {
        line = model.lineCount();
        return lineEnd();
    }

    public record Position(int line, int column) {
    }

}
