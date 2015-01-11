// PerformScriptModel.java

package apps;

import java.util.ArrayList;
import java.util.List;

/**
 * A PerformScriptModel object runs a script
 * when the program is started.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 * @see PerformScriptPanel
 */
public class PerformScriptModel {

    public PerformScriptModel() {
        fileName=null;
    }

    String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String n) {
        fileName = n;
    }

    static public void rememberObject(PerformScriptModel m) {
        l.add(m);
    }
    static public List<PerformScriptModel> rememberedObjects() {
        return l;
    }
    static List<PerformScriptModel> l = new ArrayList<PerformScriptModel>();
}


