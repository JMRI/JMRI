package jmri.jmrix.rps.rpsmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a RpsMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version
 */
public class RpsMonAction extends AbstractAction {

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
