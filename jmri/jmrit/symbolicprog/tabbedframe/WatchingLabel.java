// WatchingLabel.java

package jmri.jmrit.symbolicprog.tabbedframe;

/**
 * JLabel that watches another component, setting itself
 * invisible if when the other component is
 *
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision: 1.1 $
 *
 */
public class WatchingLabel extends javax.swing.JLabel  {
    public WatchingLabel(String n, javax.swing.JComponent c) {
        super(n);
        
        comp = c;
        self = this;
        this.name = name;
        
        comp.addComponentListener(new java.awt.event.ComponentListener(){
            public void componentHidden(java.awt.event.ComponentEvent e) {
                System.out.println(e.getComponent().getClass().getName() + " --- Hidden");
                self.setVisible(false);
            }
        
            public void componentShown(java.awt.event.ComponentEvent e) {
                System.out.println(e.getComponent().getClass().getName() + " --- Shown");
                self.setVisible(true);
            }

            public void componentMoved(java.awt.event.ComponentEvent e) {}
        
            public void componentResized(java.awt.event.ComponentEvent e) {}
        
        });
    }
    
    javax.swing.JComponent comp;
    javax.swing.JComponent self;
    String name;
}
