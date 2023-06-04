package de.cofinpro.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

public class Viewer {

    static {
        ConfigurationFactory.setConfigurationFactory(new Log4j2CustomConfigurationFactory());
    }

    private static final Logger LOG = LogManager.getLogger(Viewer.class);

    public static void main(String[] args) {
        LOG.info("\033[31mHello World!\033[0m");
    }
}
