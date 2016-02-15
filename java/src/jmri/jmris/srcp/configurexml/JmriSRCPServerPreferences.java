package jmri.jmris.srcp.configurexml;


public class JmriSRCPServerPreferences extends jmri.jmris.AbstractServerPreferences {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "SRCPServerPreferences"; // NOI18N
    static final String PORT = "port"; // NOI18N
    public JmriSRCPServerPreferences(String fileName) {
        super(fileName);
    }

    public JmriSRCPServerPreferences() {
        super();
    }

    @Override
    public int getDefaultPort() {
        return Integer.parseInt(Bundle.getMessage("JMRISRCPServerPort"));
    }

}
