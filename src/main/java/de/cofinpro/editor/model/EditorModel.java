package de.cofinpro.editor.model;

import de.cofinpro.editor.terminal.Clipping;
import de.cofinpro.editor.terminal.Cursor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorModel {

    private static final int INITIAL_LINE_LENGTH = 60;
    private static final String EMPTY_LINE = "";

    private final List<StringBuilder> lines = new ArrayList<>();

    public EditorModel() {
        lines.add(new StringBuilder(INITIAL_LINE_LENGTH));
    }

    public List<String> getClippingContent(Clipping clipping) {
        var max = Math.min(clipping.getBottom(), lines.size());
        return lines
                .subList(clipping.getTop(), max)
                .stream()
                .map(l -> getClippedLine(l, clipping)).toList();
    }

    private String getClippedLine(StringBuilder line, Clipping clipping) {
        if (line.length() <= clipping.getLeft()) {
            return EMPTY_LINE;
        }
        return line.substring(clipping.getLeft(), Math.min(line.length(), clipping.getRight()));
    }

    public String getClippedLine(int line, Clipping clipping) {
        return getClippedLine(lines.get(line - 1), clipping);
    }

    public void insertLine(int line, int column) {
        var previousLine = lines.get(line - 2);
        var lineEnd = previousLine.substring(column - 1);
        previousLine.delete(column - 1, previousLine.length());
        lines.add(line - 1, new StringBuilder(lineEnd));
    }

    public void insert(char character, Cursor cursor) {
        var lineIndex = cursor.getLine() - 1;
        if (lineIndex == lines.size()) {
            lines.add(new StringBuilder(INITIAL_LINE_LENGTH));
        }
        lines.get(lineIndex).insert(cursor.getColumn() - 1, character);
    }

    public void deleteCharAt(Cursor cursor) {
        var lineIndex = cursor.getLine() - 1;
        var newLineDeleted = cursor.getColumn() > getColsInLine(cursor.getLine());
        if (newLineDeleted) {
            if (lines.size() > 1) { // append the nextLine to this one
                lines.get(lineIndex).append(lines.get(lineIndex + 1));
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

    public Optional<Cursor.Position> search(String searchText, Cursor.Position from) {
        var index = lines.get(from.line() - 1).indexOf(searchText, from.column() - 1);
        if (index >= 0) {
            return Optional.of(new Cursor.Position(from.line(), index + 1));
        }
        for (int i = from.line(); i < lines.size(); i++) {
            index = lines.get(i).indexOf(searchText);
            if (index >= 0) {
                return Optional.of(new Cursor.Position(i + 1, index + 1));
            }
        }
        return Optional.empty();
    }

    public Optional<Cursor.Position> searchBackwards(String searchText, Cursor.Position from) {
        var index = lines.get(from.line() - 1).substring(0, from.column() - 1).lastIndexOf(searchText);
        if (index >= 0) {
            return Optional.of(new Cursor.Position(from.line(), index + 1));
        }
        for (int i = from.line() - 2; i >= 0; i--) {
            index = lines.get(i).lastIndexOf(searchText);
            if (index >= 0) {
                return Optional.of(new Cursor.Position(i + 1, index + 1));
            }
        }
        return Optional.empty();
    }

    public void saveToFile(String filename) throws IOException {
        Files.writeString(Path.of(filename), String.join( "\n", lines));
    }

    public void loadFromFile(String filename) throws IOException {
        replaceBy(Files.readString(Path.of(filename)));
    }

    private void replaceBy(String contents) {
        lines.clear();
        contents.lines().forEach(line -> lines.add(new StringBuilder(line)));
        if (contents.endsWith("\n")) {
            lines.add(new StringBuilder());
        }
    }
}
