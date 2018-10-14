package jmri.jmrit.beantable.signalmast;

import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * JFrame to create a new SignalMast
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class AddSignalMastJFrame extends JmriJFrame {

    public AddSignalMastJFrame() {
        super(Bundle.getMessage("TitleAddSignalMast"), false, true);

        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(sigMastPanel = new AddSignalMastPanel());
        pack();
    }

    public AddSignalMastJFrame(jmri.SignalMast mast) {
        super(Bundle.getMessage("TitleAddSignalMast"), false, true);

        addHelpMenu("package.jmri.jmrit.beantable.SignalMastAddEdit", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(new AddSignalMastPanel(mast));
        pack();
    }

    AddSignalMastPanel sigMastPanel = null;

    public void refresh() {
        if (sigMastPanel != null) {
            sigMastPanel.refresh();
        }
    }

}
