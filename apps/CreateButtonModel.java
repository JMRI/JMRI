// CreateButtonModel.java

package apps;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

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
 * @version     $Revision: 1.2 $
 * @see CreateButtonPanel
 */
public class CreateButtonModel extends AbstractActionModel {

    public CreateButtonModel() {
        super();
    }

    static public void rememberObject(CreateButtonModel m) {
        l.add(m);
    }
    static public List rememberedObjects() {
        return l;
    }
    static List l = new ArrayList();

}


