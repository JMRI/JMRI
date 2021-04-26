package jmri.jmrit.symbolicprog.tabbedframe;

import javax.swing.*;

/**
 * JLabel that watches another component, setting itself invisible if when the
 * other component is
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2020
 */
public class WatchingLabel extends JLabel {

    public WatchingLabel(String name, JComponent c) {
        super(name);

        comp = c;

        comp.addComponentListener(new java.awt.event.ComponentListener() {
            @Override
            public void componentHidden(java.awt.event.ComponentEvent e) {
                WatchingLabel.this.setVisible(false);
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                WatchingLabel.this.setVisible(true);
            }

            @Override
            public void componentMoved(java.awt.event.ComponentEvent e) {
            }

            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
            }

        });

        // set initial status
        setVisible(c.isVisible());
    }

    public JComponent getWatcher() { return this; }
    public JComponent getWatched() { return comp; }
    
    JComponent comp;
}
