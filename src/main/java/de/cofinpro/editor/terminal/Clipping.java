package de.cofinpro.editor.terminal;

import lombok.Getter;

@Getter
public class Clipping {

    int top; // line number index - starting with 0
    int bottom; // line index of bottom (not included)
    int left;
    int right;
    private final RefreshListener refreshListener;
    private final int horizontalScrollDist;
    private final int verticalScrollDist;

    public Clipping(int top, int bottom, int left, int right, RefreshListener refreshListener) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.refreshListener = refreshListener;
        horizontalScrollDist = (right - left) / 2;
        verticalScrollDist = (bottom - top) / 2;
    }

    public void scrollDown() {
        top += verticalScrollDist;
        bottom += verticalScrollDist;
        refreshListener.refresh();
    }

    public void scrollUp() {
        var scrollDist = Math.min(top, verticalScrollDist);
        top -= scrollDist;
        bottom -= scrollDist;
        refreshListener.refresh();
    }

    public void scrollRight() {
        left += horizontalScrollDist;
        right += horizontalScrollDist;
        refreshListener.refresh();
    }

    public void scrollLeft() {
        var scrollDist = Math.min(left, horizontalScrollDist);
        left -= scrollDist;
        right -= scrollDist;
        refreshListener.refresh();
    }

    public void left() {
        right -= left;
        left = 0;
        refreshListener.refresh();
    }


    public int getDisplayCol(int column) {
        return column - left;
    }

    public void centerHorizontal(int column) {
        var dist = column - right + horizontalScrollDist;
        left += dist;
        right += dist;
        refreshListener.refresh();
    }
}
