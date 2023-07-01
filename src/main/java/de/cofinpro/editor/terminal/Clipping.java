package de.cofinpro.editor.terminal;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Clipping {

    private int top; // line number index - starting with 0
    private int bottom; // line index of bottom (not included)
    private int left;
    private int right;
    private final Refreshable terminal;
    private int horizontalScrollDist;
    private int verticalScrollDist;

    public Clipping(int rows, int cols, Refreshable terminal) {
        this.terminal = terminal;
        resize(rows, cols, null);
    }

    public void resize(int rows, int cols, Cursor cursor) {
        this.bottom = top + rows - 1;
        this.right = left + cols;
        horizontalScrollDist = (right - left) / 2;
        verticalScrollDist = (bottom - top) / 2;
        if (Objects.nonNull(cursor)) {
            setPosition(cursor);
        }
    }

    public void setPosition(Cursor cursor) {
        boolean refresh = false;
        if (cursor.line <= top || cursor.line >= bottom) {
            centerVertical(cursor.line);
            refresh = true;
        }
        if (cursor.column <= left || cursor.column >= right) {
            centerHorizontal(cursor.column);
            refresh = true;
        }
        if (refresh) {
            terminal.refresh();
            return;
        }
        terminal.refreshLine();
    }

    private void centerVertical(int line) {
        var newTop = Math.max(0, line - verticalScrollDist);
        bottom += newTop - top;
        top = newTop;
    }

    private void centerHorizontal(int column) {
        var newLeft = Math.max(0, column - 1 - horizontalScrollDist);
        right += newLeft - left;
        left = newLeft;
    }
}
