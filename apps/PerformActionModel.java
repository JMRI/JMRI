// PerformActionModel.java

package apps;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * A PerformActionModel object invokes a Swing Action
 * when the program is started.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.1 $
 * @see PerformActionPanel
 */
public class PerformActionModel {

    public PerformActionModel() {
        className="";
    }

    String className;

    public String getClassName() {
        return className;
    }

    public String getName() {
        for (int i =0; i< nameList().length; i++)
            if (classList()[i].getName().equals(className))
                return nameList()[i];
        return null;
    }

    public void setName(String n) {
        for (int i =0; i< nameList().length; i++)
            if (nameList()[i].equals(n))
                className = classList()[i].getName();
    }

    public void setClassName(String n) {
        className = n;
    }

    static public String[] nameList() {
        return new String[] {
            "Open memory monitor",
            "Start LocoNet Server"
        };
    }

    static public Class[] classList() {
        return new Class[] {
            jmri.jmrit.MemoryFrameAction.class,
            jmri.jmrix.loconet.locormi.LnMessageServerAction.class
        };
    }

    static public void rememberObject(PerformActionModel m) {
        l.add(m);
    }
    static public List rememberedObjects() {
        return l;
    }
    static List l = new ArrayList();

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformActionModel.class.getName());
}


