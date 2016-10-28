package jmri.jmris.simpleserver;

import java.io.File;
import jmri.InstanceManager;
import jmri.util.FileUtil;

public class SimpleServerManager {

    private SimpleServerPreferences preferences;
    private SimpleServer server;

    private SimpleServerManager() {
        if (InstanceManager.getNullableDefault(SimpleServerPreferences.class) == null) {
            String fileName = FileUtil.getUserFilesPath() + "networkServices" + File.separator + "SimpleServer.xml";
            if ((new File(fileName)).exists()) {
                InstanceManager.store(new SimpleServerPreferences(fileName), SimpleServerPreferences.class); // NOI18N
            } else {
                InstanceManager.store(new SimpleServerPreferences(), SimpleServerPreferences.class);
            }
        }
        preferences = InstanceManager.getDefault(SimpleServerPreferences.class);
    }

    public static SimpleServerManager getInstance() {
        if (InstanceManager.getNullableDefault(SimpleServerManager.class) == null) {
            InstanceManager.store(new SimpleServerManager(), SimpleServerManager.class); // NOI18N
        }
        return InstanceManager.getDefault(SimpleServerManager.class);
    }

    public SimpleServerPreferences getPreferences() {
        if (preferences == null) {
            preferences = new SimpleServerPreferences();
        }
        return preferences;
    }

    public static SimpleServerPreferences getSimpleServerPreferences() {
        return getInstance().getPreferences();
    }

    public SimpleServer getServer() {
        if (server == null) {
            server = new SimpleServer(this.getPreferences().getPort());
        }
        return server;
    }

    public static SimpleServer getSimpleServer() {
        return getInstance().getServer();
    }
}
