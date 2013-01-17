// DecoderPro3Window.java
package apps.gui3.dp3;

import jmri.Application;
import jmri.jmrit.roster.swing.RosterFrame;

/**
 * Standalone DecoderPro3 Window.
 */
public class DecoderPro3Window extends RosterFrame {

    /**
     * Loads Decoder Pro 3 with the default set of menus and toolbars
     */
    public DecoderPro3Window() {
        super(Application.getApplicationName());
    }

    /**
     * Loads Decoder Pro 3 with specific menu and toolbar files
     */
    public DecoderPro3Window(String menuFile, String toolbarFile) {
        super(Application.getApplicationName(),
                menuFile,
                toolbarFile);
        this.setNewWindowAction(new DecoderPro3Action("newWindow", this));
    }

    // for some reason, the super implementation does not get called automatically
    @Override
    public void remoteCalls(String[] args) {
        super.remoteCalls(args);
    }
}