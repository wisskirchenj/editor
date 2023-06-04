package de.cofinpro.editor;

import com.sun.jna.Native;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

import java.io.IOException;

public class Viewer {

    static {
        ConfigurationFactory.setConfigurationFactory(new Log4j2CustomConfigurationFactory());
    }

    public static void main(String[] args) throws IOException {
        Native.setProtected(true);
        var terminal = new Terminal();
        terminal.keyPressLoop();
    }
}
