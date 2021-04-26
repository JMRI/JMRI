package jmri.jmris.simpleserver;

import java.io.File;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.util.FileUtil;

public class SimpleServerManager implements InstanceManagerAutoDefault {

    private SimpleServerPreferences preferences;
    private SimpleServer server;
    public SimpleServerManager(){
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

    /**
     *
     * @return the instance of the SimpleServerManager
     * @deprecated since 4.19.5.  Use InstanceManager.getDefault(SimpleServerManager.class) instead
     */
    @Deprecated
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
        return InstanceManager.getDefault(SimpleServerManager.class).getPreferences();
    }

    public SimpleServer getServer() {
        if (server == null) {
            server = new SimpleServer(this.getPreferences().getPort());
        }
        return server;
    }

    public static SimpleServer getSimpleServer() {
        return InstanceManager.getDefault(SimpleServerManager.class).getServer();
    }
}
