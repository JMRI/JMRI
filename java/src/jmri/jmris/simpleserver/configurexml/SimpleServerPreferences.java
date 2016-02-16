package jmri.jmris.simpleserver.configurexml;


public class SimpleServerPreferences extends jmri.jmris.AbstractServerPreferences {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "SimpleServerPreferences"; // NOI18N
    static final String PORT = "port"; // NOI18N
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
