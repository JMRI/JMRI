// EditorFrame.java
package jmri.jmrix.loconet.sdfeditor;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import jmri.jmrix.loconet.sdf.SdfBuffer;
import jmri.util.JmriJFrame;

/**
 * Frame for editing Digitrax SDF files.
 * <P>
 * This is just an enclosure for the EditorPane, which does the real work.
 * <P>
 * This handles file read/write.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version $Revision$
 */
public class EditorFrame extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = 7106886649627618274L;

    // GUI member declarations
    EditorPane pane;

    ResourceBundle res;

    public EditorFrame(SdfBuffer buff) {
        super(ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor").getString("TitleEditor"));

        // Its unfortunate that we have to read that bundle twice, but it's due to Java init order
        res = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor");

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add panel
        pane = new EditorPane();
        pane.addSdf(buff);
        getContentPane().add(pane);

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.loconet.sdfeditor.EditorFrame", true);

        pack();

    }

    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}
