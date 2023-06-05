package de.cofinpro.editor.terminal;

class AnsiEscape {

    static final String ERASE_SEQ = "\033[%dJ";
    static final String POS_SEQ = "\033[%sH";

    private AnsiEscape() {
        // no instances
    }

    static String positionCursorTopLeft() {
        return POS_SEQ.formatted("");
    }

    static String positionCursor(int row, int column) {
        var rowColumn = "%d;%d".formatted(row, column);
        return POS_SEQ.formatted(rowColumn);
    }

    static String erase(EraseMode mode) {
        return ERASE_SEQ.formatted(mode.ordinal());
    }

    static String magenta(String message) {
        return rendition(message, 35);
    }

    static String green(String message) {
        return rendition(message, 32);
    }

    static String red(String message) {
        return rendition(message, 31);
    }

    static String inverted(String message) {
        return rendition(message, 7);
    }

    private static String rendition(String message, int colorCode) {
        return "\033[%dm%s\033[0m".formatted(colorCode, message);
    }

    enum EraseMode {
        CURSOR_TO_END,
        CURSOR_TO_BEGIN,
        ALL
    }
}
