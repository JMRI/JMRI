// PerformFileModel.java

package apps;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * A PerformFileModel object loads an xml file
 * when the program is started.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision: 1.2 $
 * @see PerformFilePanel
 */
public class PerformFileModel {

    public PerformFileModel() {
        fileName=null;
    }

    String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String n) {
        fileName = n;
    }

    static public void rememberObject(PerformFileModel m) {
        l.add(m);
    }
    static public List rememberedObjects() {
        return l;
    }
    static List l = new ArrayList();

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformFileModel.class.getName());
}


