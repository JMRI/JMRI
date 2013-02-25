package jmri.jmris.json;

import java.io.File;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonServerManager {

    static private JsonServerManager instance = null;
    private JsonServerPreferences preferences;
    private JsonServer server;
    static Logger log = LoggerFactory.getLogger(JsonServer.class.getName());

    private JsonServerManager() {
        if (InstanceManager.getDefault(JsonServerPreferences.class) == null) {
            InstanceManager.store(new JsonServerPreferences(FileUtil.getUserFilesPath() + "networkServices" + File.separator + "JsonServerPreferences.xml"), JsonServerPreferences.class); // NOI18N
        }
        preferences = InstanceManager.getDefault(JsonServerPreferences.class);
    }

    public static JsonServerManager getInstance() {
        if (instance == null) {
            instance = new JsonServerManager();
        }
        return instance;
    }

    public JsonServerPreferences getPreferences() {
        if (preferences == null) {
            preferences = new JsonServerPreferences();
        }
        return preferences;
    }

    public static JsonServerPreferences getJsonServerPreferences() {
        return getInstance().getPreferences();
    }

    public JsonServer getServer() {
        if (server == null) {
            server = new JsonServer(this.getPreferences().getPort(), this.getPreferences().getHeartbeatInterval());
        }
        return server;
    }

    public static JsonServer getJsonServer() {
        return getInstance().getServer();
    }
}
