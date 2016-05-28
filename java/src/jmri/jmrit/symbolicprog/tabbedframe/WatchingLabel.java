package jmri.jmrit.symbolicprog.tabbedframe;

/**
 * JLabel that watches another component, setting itself invisible if when the
 * other component is
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 */
public class WatchingLabel extends javax.swing.JLabel {

    public WatchingLabel(String name, javax.swing.JComponent c) {
        super(name);

        comp = c;
        self = this;

        comp.addComponentListener(new java.awt.event.ComponentListener() {
            public void componentHidden(java.awt.event.ComponentEvent e) {
                self.setVisible(false);
            }

            public void componentShown(java.awt.event.ComponentEvent e) {
                self.setVisible(true);
            }

            public void componentMoved(java.awt.event.ComponentEvent e) {
            }

            public void componentResized(java.awt.event.ComponentEvent e) {
            }

        });

        // set initial status
        self.setVisible(c.isVisible());
    }

    javax.swing.JComponent comp;
    javax.swing.JComponent self;
}
