// PerformFileModel.java
package apps;

/**
 * A PerformFileModel object loads an xml file when the program is started.
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @see apps.startup.PerformFileModelFactory
 */
public class PerformFileModel implements StartupModel {

    public PerformFileModel() {
        fileName = null;
    }

    String fileName;

    @Override
    public String getName() {
        return this.getFileName();
    }
    
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String n) {
        fileName = n;
    }

    @Override
    public void setName(String name) {
        this.setFileName(name);
    }
}
