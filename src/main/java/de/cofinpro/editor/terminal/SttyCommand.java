package de.cofinpro.editor.terminal;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static de.cofinpro.editor.terminal.AnsiEscape.red;

@Slf4j
public class SttyCommand {

    String run() {
        try {
            var process = new ProcessBuilder("stty", "-f", "/dev/tty", "size").start();
            return new String(process.getInputStream().readAllBytes());
        } catch (IOException e) {
            log.error(red("stty Command failed!"));
            throw new CommandException();
        }
    }

    static class CommandException extends RuntimeException {
    }
}
