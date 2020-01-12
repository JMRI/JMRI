package jmri.jmrix.loconet.hexfile;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a LnHexFileFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class LnHexFileAction extends AbstractAction {

    public LnHexFileAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a LnHexFileFrame
        HexFileFrame f = new HexFileFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("starting HexFileFrame exception: " + ex.toString());
        }
        f.pack();
        f.setVisible(true);
        // it connects to the LnTrafficController when the right button is pressed

    }

    private final static Logger log = LoggerFactory.getLogger(LnHexFileAction.class);

}
