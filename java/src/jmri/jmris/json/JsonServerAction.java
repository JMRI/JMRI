package jmri.jmris.json;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 
 * @author Randall Wood
 * @deprecated since 4.19.2; use {@link jmri.server.json.JsonServerAction} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated for replacement.")
public class JsonServerAction extends jmri.server.json.JsonServerAction {

    public JsonServerAction(String s) {
        super(s);
    }

    public JsonServerAction() {
        super();
    }
}
