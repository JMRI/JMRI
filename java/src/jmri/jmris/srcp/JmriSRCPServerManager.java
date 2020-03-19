package jmri.jmris.srcp;

import java.io.File;
import jmri.InstanceManagerDelegate;
import jmri.util.FileUtil;

public class JmriSRCPServerManager {

    static private JmriSRCPServerManager instance = null;
    private JmriSRCPServerPreferences preferences;
    private JmriSRCPServer server;
    private InstanceManagerDelegate instanceManager;

    private JmriSRCPServerManager(InstanceManagerDelegate instanceManager) {
        this.instanceManager = instanceManager;
        if (instanceManager.getNullableDefault(JmriSRCPServerPreferences.class) == null) {
            String fileName = FileUtil.getUserFilesPath() + "networkServices" + File.separator + "JmriSRCPServerPreferences.xml";
            if ((new File(fileName)).exists()) {
                instanceManager.store(new JmriSRCPServerPreferences(fileName), JmriSRCPServerPreferences.class); // NOI18N
            } else {
                instanceManager.store(new JmriSRCPServerPreferences(), JmriSRCPServerPreferences.class); // NOI18N
            }
        }
    }

    public static synchronized JmriSRCPServerManager getInstance() {
        if (instance == null) {
            instance = new JmriSRCPServerManager(new InstanceManagerDelegate());
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
