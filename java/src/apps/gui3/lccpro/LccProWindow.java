package apps.gui3.lccpro;

import apps.gui3.Apps3;
import jmri.Application;
import jmri.jmrix.openlcb.swing.lccpro.LccProFrame;

/**
 * Standalone LccPro Window.
 */
public class LccProWindow extends LccProFrame {

    /**
     * Loads LccPro with the default set of menus and toolbars
     */
    public LccProWindow() {
        super(Application.getApplicationName());
    }

    /**
     * Loads LccPro with specific menu and toolbar files.
     *
     * @param menuFile XML file with menu structure
     * @param toolbarFile XML file with toolbar structure
     */
    public LccProWindow(String menuFile, String toolbarFile) {
        super(Application.getApplicationName(),
                menuFile,
                toolbarFile);
        this.setNewWindowAction(new LccProAction("newWindow", this));
    }

    // for some reason, the super implementation does not get called automatically
    @Override
    public void remoteCalls(String[] args) {
        super.remoteCalls(args);
    }

    @Override
    protected void additionsToToolBar() {
        // This buttonspace value may return null if the LccPro window has 
        // been called from the traditional JMRI menu frame
        if (Apps3.buttonSpace() != null) {
            getToolBar().add(Apps3.buttonSpace());
        }
        super.additionsToToolBar();
    }

}
