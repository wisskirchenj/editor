package de.cofinpro.editor.terminal;

import de.cofinpro.editor.model.EditorModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiFunction;

import static de.cofinpro.editor.terminal.AnsiEscape.BACKSPACE;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_A;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_E;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_F;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_L;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_Q;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_R;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_S;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_V;
import static de.cofinpro.editor.terminal.AnsiEscape.CTRL_W;
import static de.cofinpro.editor.terminal.AnsiEscape.ESC;
import static de.cofinpro.editor.terminal.AnsiEscape.EraseMode.ALL;
import static de.cofinpro.editor.terminal.AnsiEscape.RETURN;
import static de.cofinpro.editor.terminal.AnsiEscape.back;
import static de.cofinpro.editor.terminal.AnsiEscape.erase;
import static de.cofinpro.editor.terminal.AnsiEscape.eraseLine;
import static de.cofinpro.editor.terminal.AnsiEscape.inverted;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursor;
import static de.cofinpro.editor.terminal.AnsiEscape.positionCursorTopLeft;

@Slf4j
public class Editor implements Refreshable {

    private static final String STATUS_PREFIX = " Jürgen's Editor: (L%d C%d) ";
    private final EditorModel model;
    private final Clipping clipping;
    private final Cursor cursor;
    private String filename = "";
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

    @SneakyThrows
    public void run() {
        refresh();
        int key = System.in.read();
        while (key != CTRL_Q) {
            switch (key) {
                case BACKSPACE -> backspace();
                case RETURN -> carriageReturn();
                case ESC -> readEscapeSequence();
                case CTRL_W -> resizeWindow();
                case CTRL_A -> beginOfLine();
                case CTRL_E -> endOfLine();
                case CTRL_F -> find(Direction.DOWN);
                case CTRL_R -> find(Direction.UP);
                case CTRL_V -> scroll(Direction.DOWN);
                case CTRL_S -> new FileHandler().saveBuffer();
                case CTRL_L -> new FileHandler().loadBuffer();
                default -> print(key);
            }
            key = System.in.read();
        }
        close();
    }

    private void readEscapeSequence() throws IOException {
        int second = System.in.read();
        switch (second) {
            case '[' -> processCsiSequence();
            case 'v' -> scroll(Direction.UP);
            case '<' -> beginOfBuffer();
            case '>' -> endOfBuffer();
            default -> print(second);
        }
    }

    private void processCsiSequence() throws IOException {
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

    private void find(Direction direction) {
        var searchText = readFromStatusbar("Enter search text:");
        BiFunction<String, Cursor.Position, Optional<Cursor.Position>> searchMethod = direction == Direction.UP
                        ? model::searchBackwards
                        : model::search;
        searchMethod.apply(searchText, cursor.getPosition()).ifPresentOrElse(
                pos -> incrementalSearch(pos, searchText),
                () -> log.info(updateDisplayWithStatus("Not found!"))
        );
    }

    @SneakyThrows
    private void incrementalSearch(Cursor.Position pos, String searchText) {
        clipping.setPosition(cursor.setPosition(pos));
        log.info(updateDisplayWithStatus("n -> find next; p -> find previous; q -> quit"));
        int key = System.in.read();
        while (key != 'q') {
            if (key == 'n' && !findForward(searchText) || key == 'p' && !findBackward(searchText)) {
                break;
            }
            clipping.setPosition(cursor);
            log.info(updateDisplayWithStatus("n -> find next; p -> find previous; q -> quit"));
            key = System.in.read();
        }
        log.info(key == 'q' ? updateDisplayAndStatus() : updateDisplayWithStatus("Not found!"));
    }

    private boolean findBackward(String searchText) {
        var positionOpt = model.searchBackwards(searchText, cursor.getPosition());
        if (positionOpt.isEmpty()) {
            return false;
        } else {
            cursor.setPosition(positionOpt.get());
        }
        return true;
    }

    private boolean findForward(String searchText) {
        var positionOpt = model.search(searchText, cursor.forward().getPosition());
        if (positionOpt.isEmpty()) {
            cursor.back();
            return false;
        } else {
            cursor.setPosition(positionOpt.get());
        }
        return true;
    }

    private void beginOfLine() {
        clipping.setPosition(cursor.lineBegin());
    }

    private void beginOfBuffer() {
        clipping.setPosition(cursor.jumpBeginOfBuffer());
    }

    private void endOfLine() {
        clipping.setPosition(cursor.lineEnd());
    }

    private void endOfBuffer() {
        clipping.setPosition(cursor.jumpEndOfBuffer());
    }

    private void scroll(Direction direction) {
        cursor.jumpToLine(direction == Direction.UP
                ? Math.max(1, cursor.line - rows + 1)
                : Math.min(model.lineCount(), cursor.line + rows - 1));
        clipping.setPosition(cursor);
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
        clipping.setPosition(cursor.forward());
    }

    private void carriageReturn() {
        var column = cursor.column;
        model.insertLine(cursor.carriageReturn().line, column);
        clipping.setPosition(cursor);
        refresh(); // needed as two lines need refresh (not only the one which is handled by setPosition)
    }

    private void backspace() {
        if (cursor.isAtStartOfBuffer()) {
            return;
        }
        model.deleteCharAt(cursor.back());
        clipping.setPosition(cursor);
        refresh(); // needed as two lines need refresh (not only the one which is handled by setPosition)
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
        return updateDisplayWithStatus(filename);
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

    @SneakyThrows
    private String readFromStatusbar(String prompt) {
        log.info(positionCursor(rows, 1)
                 + inverted(prompt + " ".repeat(cols - prompt.length()))
                 + positionCursor(rows, prompt.length() + 2));
        var builder = new StringBuilder();
        int key = System.in.read();
        while (key != RETURN) {
            if (!builder.isEmpty() && key == BACKSPACE) {
                log.info(back() + inverted(" ") + back());
                builder.deleteCharAt(builder.length() - 1);
            } else if (key > 31 && key < BACKSPACE) { // allowed chars
                log.info(inverted("{}"), (char) key);
                builder.append((char) key);
            }
            key = System.in.read();
        }
        return builder.toString();
    }

    private enum Direction {
        UP,
        DOWN
    }

    private class FileHandler {
        private void loadBuffer() {
            filename = readFromStatusbar("Enter filename:");
            try {
                model.loadFromFile(filename);
                positionCursorTopLeft();
                clipping.setPosition(cursor.topLeft());
                refresh();
            } catch (IOException e) {
                filename = "";
                log.info(updateDisplayWithStatus(" - " + e));
            }
        }

        private void saveBuffer() {
            filename = readFromStatusbar("Enter filename:");
            try {
                model.saveToFile(filename);
                refresh();
            } catch (IOException e) {
                filename = "";
                log.info(updateDisplayWithStatus(" - " + e));
            }
        }

    }
}
