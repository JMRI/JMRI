package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JmriJFrame;

/**
 * The frame to hold an AssociateTag panel
 *
 * @author J. Scott Walton Copyright (C) 2022
 */
public class AssociateFrame extends JmriJFrame {

    AssociateTag panel;

    public AssociateFrame(AssociateTag tagPanel, String title) {
        super(false, false);
        panel = tagPanel;
        setEscapeKeyClosesWindow(true);
        setTitle(title);
        tagPanel.setParentFrame(this);
    }

    @Override
    public void initComponents() {
        panel.initComponents();
        getContentPane().add(panel);
        pack();
        this.setVisible(true);
    }


}
