package jmri.jmris.simpleserver;

import java.io.File;
import jmri.InstanceManagerDelegate;
import jmri.util.FileUtil;

public class SimpleServerManager {

    private SimpleServerPreferences preferences;
    private SimpleServer server;
    private final InstanceManagerDelegate instanceManagerDelegate;


    private SimpleServerManager(InstanceManagerDelegate instanceManagerDelegate) {
        this.instanceManagerDelegate = instanceManagerDelegate;
        if (instanceManagerDelegate.getNullableDefault(SimpleServerPreferences.class) == null) {
            String fileName = FileUtil.getUserFilesPath() + "networkServices" + File.separator + "SimpleServer.xml";
            if ((new File(fileName)).exists()) {
                instanceManagerDelegate.store(new SimpleServerPreferences(fileName), SimpleServerPreferences.class); // NOI18N
            } else {
                instanceManagerDelegate.store(new SimpleServerPreferences(), SimpleServerPreferences.class);
            }
        }
        preferences = instanceManagerDelegate.getDefault(SimpleServerPreferences.class);
    }

    public static SimpleServerManager getInstance() {
        InstanceManagerDelegate instanceManagerDelegate = new InstanceManagerDelegate();
        if (instanceManagerDelegate.getNullableDefault(SimpleServerManager.class) == null) {
            instanceManagerDelegate.store(new SimpleServerManager(instanceManagerDelegate), SimpleServerManager.class); // NOI18N
        }
        return instanceManagerDelegate.getDefault(SimpleServerManager.class);
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
            server = new SimpleServer(this.getPreferences().getPort(),instanceManagerDelegate);
        }
        return server;
    }

    public static SimpleServer getSimpleServer() {
        return getInstance().getServer();
    }
}
