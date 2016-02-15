package jmri.jmris.simpleserver;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmris.simpleserver.configurexml.SimpleServerPreferences;
import jmri.util.FileUtil;

public class SimpleServerManager {

    static private SimpleServerManager instance = null;
    private SimpleServerPreferences preferences;
    private SimpleServer server;
    private SimpleServerManager() {
        if (InstanceManager.getDefault(SimpleServerPreferences.class) == null) {
            InstanceManager.store(new SimpleServerPreferences(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "SimpleServer.xml"), SimpleServerPreferences.class); // NOI18N
        }
        preferences = InstanceManager.getDefault(SimpleServerPreferences.class);
    }

    public static SimpleServerManager getInstance() {
        if (instance == null) {
            instance = new SimpleServerManager();
        }
        return instance;
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
