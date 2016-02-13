package jmri.jmris.simpleserver.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleServerPreferences extends jmri.jmris.AbstractServerPreferences {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "SimpleServerPreferences"; // NOI18N
    static final String PORT = "port"; // NOI18N
    // initial defaults if prefs not found
    private int port = DEFAULT_PORT;
    // as loaded prefences
    private int asLoadedPort = DEFAULT_PORT;
    private final static Logger log = LoggerFactory.getLogger(SimpleServerPreferences.class);

    public SimpleServerPreferences(String fileName) {
        super(fileName);
    }

    public SimpleServerPreferences() {
        super();
    }

    @Override
    public int getDefaultPort() {
        return Integer.parseInt(Bundle.getMessage("SimpleServerPort"));
    }

}
