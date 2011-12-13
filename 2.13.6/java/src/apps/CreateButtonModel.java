// CreateButtonModel.java

package apps;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a button
 * when the program is started.
 * <P>
 * The list of actions available is defined in the
 * {@link AbstractActionModel} superclass.
 * <P>
 * This is a separate class, even though it
 * has no additional behavior, so that persistance
 * systems realize the type of data being stored.
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 * @see CreateButtonPanel
 */
public class CreateButtonModel extends AbstractActionModel {

    public CreateButtonModel() {
        super();
    }

    static public void rememberObject(CreateButtonModel m) {
        l.add(m);
    }
    static public List<CreateButtonModel> rememberedObjects() {
        return l;
    }
    static List<CreateButtonModel> l = new ArrayList<CreateButtonModel>();

}


