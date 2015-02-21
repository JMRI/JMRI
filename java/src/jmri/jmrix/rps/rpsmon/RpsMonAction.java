/**
 * RpsMonAction.java
 *
 * Description:	Swing action to create and register a RpsMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version
 */
package jmri.jmrix.rps.rpsmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class RpsMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8976860321295273855L;

    public RpsMonAction(String s) {
        super(s);
    }

    public RpsMonAction() {
        this("RPS Monitor");
    }

    public void actionPerformed(ActionEvent e) {
        RpsMonFrame f = new RpsMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
        }
        f.setVisible(true);

    }

}


/* @(#)RpsMonAction.java */
