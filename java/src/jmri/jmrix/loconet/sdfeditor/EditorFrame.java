package jmri.jmrix.loconet.sdfeditor;

import javax.swing.BoxLayout;
import jmri.jmrix.loconet.sdf.SdfBuffer;
import jmri.util.JmriJFrame;

/**
 * Frame for editing Digitrax SDF files.
 * <p>
 * This is just an enclosure for the EditorPane, which does the real work.
 * <p>
 * This handles file read/write.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class EditorFrame extends JmriJFrame {

    // GUI member declarations
    EditorPane pane;

    public EditorFrame(SdfBuffer buff) {
        super(Bundle.getMessage("TitleEditor"));

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

    @Override
    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}
