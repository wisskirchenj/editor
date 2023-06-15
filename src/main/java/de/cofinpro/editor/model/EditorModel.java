package de.cofinpro.editor.model;

import de.cofinpro.editor.terminal.Clipping;
import de.cofinpro.editor.terminal.Editor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditorModel {

    private static final int INITIAL_LINE_LENGTH = 60;
    private static final String EMPTY_LINE = "";

    private final List<StringBuilder> lines = new ArrayList<>();
    private final Clipping clipping;

    public EditorModel(Clipping clipping) {
        this.clipping = clipping;
        lines.add(new StringBuilder(INITIAL_LINE_LENGTH));
    }

    public List<String> getClippingContent() {
        var max = Math.min(clipping.getBottom(), lines.size());
        return lines
                .subList(clipping.getTop(), max)
                .stream()
                .map(this::getClippedLine).toList();
    }

    private String getClippedLine(StringBuilder line) {
        if (line.length() <= clipping.getLeft()) {
            return EMPTY_LINE;
        }
        return line.substring(clipping.getLeft(), Math.min(line.length(), clipping.getRight()));
    }

    public String getClippedLine(int line) {
        return getClippedLine(lines.get(line - 1));
    }

    public void insertLine(int line) {
        lines.add(line - 1, new StringBuilder(INITIAL_LINE_LENGTH));
    }

    public void insert(char character, Editor.Cursor cursor) {
        var lineIndex = cursor.getLine() - 1;
        if (lineIndex == lines.size()) {
            lines.add(new StringBuilder(INITIAL_LINE_LENGTH));
        }
        lines.get(lineIndex).insert(cursor.getColumn() - 1, character);
    }

    public void deleteCharAt(Editor.Cursor cursor) {
        var lineIndex = cursor.getLine() - 1;
        if (cursor.getColumn() > getColsInLine(lineIndex + 1)) {
            if (lines.size() > lineIndex + 1) {
                lines.remove(lineIndex + 1);
            }
        } else {
            lines.get(lineIndex).deleteCharAt(cursor.getColumn() - 1);
        }
    }

    public int getColsInLine(int line) {
        return lines.get(line - 1).length();
    }

    public int lineCount() {
        return lines.size();
    }
}
