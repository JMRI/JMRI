package jmri.jmris.srcp;

import java.io.File;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmris.srcp.configurexml.JmriSRCPServerPreferences;

public class JmriSRCPServerManager {

    static private JmriSRCPServerManager instance = null;
    private JmriSRCPServerPreferences preferences;
    private JmriSRCPServer server;
    private final static Logger log = LoggerFactory.getLogger(JmriSRCPServer.class.getName());

    private JmriSRCPServerManager() {
        if (InstanceManager.getDefault(JmriSRCPServerPreferences.class) == null) {
            InstanceManager.store(new JmriSRCPServerPreferences(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "JmriSRCPServerPreferences.xml"), JmriSRCPServerPreferences.class); // NOI18N
        }
        preferences = InstanceManager.getDefault(JmriSRCPServerPreferences.class);
    }

    public static JmriSRCPServerManager getInstance() {
        if (instance == null) {
            instance = new JmriSRCPServerManager();
        }
        return instance;
    }

    public JmriSRCPServerPreferences getPreferences() {
        if (preferences == null) {
            preferences = new JmriSRCPServerPreferences();
        }
        return preferences;
    }

    public static JmriSRCPServerPreferences getJmriSRCPServerPreferences() {
        return getInstance().getPreferences();
    }

    public JmriSRCPServer getServer() {
        if (server == null) {
            server = new JmriSRCPServer(this.getPreferences().getPort());
        }
        return server;
    }

    public static JmriSRCPServer getJmriSRCPServer() {
        return getInstance().getServer();
    }
}
