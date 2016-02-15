package jmri.jmris.srcp.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmriSRCPServerPreferences extends jmri.jmris.AbstractServerPreferences {

    public static final int DEFAULT_PORT = 2056;
    static final String XML_PREFS_ELEMENT = "SRCPServerPreferences"; // NOI18N
    static final String PORT = "port"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(JmriSRCPServerPreferences.class);

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
