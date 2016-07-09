package jmri.jmris.json;

import jmri.InstanceManager;

/**
 * This class is obsolete. Use {@link jmri.jmris.json.JsonServer} and
 * {@link jmri.jmris.json.JsonServerPreferences} directly.
 *
 * @author Randall Wood
 * @deprecated This class will be removed during the JMRI 5.x development cycle.
 */
@Deprecated
public class JsonServerManager {

    /**
     *
     * @return The default JsonServerManager instance.
     * @deprecated Use {@link jmri.jmris.json.JsonServer#getDefault()} and
     * {@link jmri.jmris.json.JsonServerPreferences#getDefault()} instead.
     */
    @Deprecated
    public static JsonServerManager getInstance() {
        if (InstanceManager.getOptionalDefault(JsonServerManager.class) == null) {
            InstanceManager.store(new JsonServerManager(), JsonServerManager.class);
        }
        return InstanceManager.getDefault(JsonServerManager.class);
    }

    /**
     *
     * @return The default JsonServerPreferences instance.
     * @deprecated Use
     * {@link jmri.jmris.json.JsonServerPreferences#getDefault()} instead.
     */
    @Deprecated
    public JsonServerPreferences getPreferences() {
        return JsonServerPreferences.getDefault();
    }

    /**
     *
     * @return The default JsonServerPreferences instance.
     * @deprecated Use
     * {@link jmri.jmris.json.JsonServerPreferences#getDefault()} instead.
     */
    @Deprecated
    public static JsonServerPreferences getJsonServerPreferences() {
        return JsonServerPreferences.getDefault();
    }

    /**
     *
     * @return The default JsonServer instance.
     * @deprecated Use {@link jmri.jmris.json.JsonServer#getDefault()} instead.
     */
    @Deprecated
    public JsonServer getServer() {
        return JsonServer.getDefault();
    }

    /**
     *
     * @return The default JsonServer instance.
     * @deprecated Use {@link jmri.jmris.json.JsonServer#getDefault()} instead.
     */
    @Deprecated
    public static JsonServer getJsonServer() {
        return JsonServer.getDefault();
    }
}
