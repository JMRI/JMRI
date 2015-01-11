// PerformActionModel.java

package apps;

import java.util.ArrayList;
import java.util.List;

/**
 * Invokes a Swing Action
 * when the program is started.
 * <P>
 * The list of actions available is defined in the
 * {@link AbstractActionModel} superclass.
 * <P>
 * This is a separate class, even though it
 * has no additional behavior, so that persistance
 * systems realize the type of data being stored.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 * @see PerformActionPanel
 */
public class PerformActionModel extends AbstractActionModel {

    public PerformActionModel() {
        super();
    }
    static public void rememberObject(PerformActionModel m) {
        l.add(m);
    }
    static public List<PerformActionModel> rememberedObjects() {
        return l;
    }
    static List<PerformActionModel> l = new ArrayList<PerformActionModel>();

}


