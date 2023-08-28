package de.cofinpro.editor;

import com.sun.jna.Native;
import de.cofinpro.editor.config.Log4j2CustomConfigurationFactory;
import de.cofinpro.editor.terminal.Editor;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

public class Viewer {

    static {
        ConfigurationFactory.setConfigurationFactory(new Log4j2CustomConfigurationFactory());
    }

    public static void main(String[] args) {
        Native.setProtected(true);
        new Editor().run();
    }
}
