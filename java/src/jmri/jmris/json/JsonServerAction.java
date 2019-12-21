package jmri.jmris.json;

/**
 * 
 * @author Randall Wood
 * @deprecated since 4.19.2; use {@link jmri.server.json.JsonServerAction} instead
 */
@Deprecated
public class JsonServerAction extends jmri.server.json.JsonServerAction {

    public JsonServerAction(String s) {
        super(s);
    }

    public JsonServerAction() {
        super();
    }
}
