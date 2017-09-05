package jmri.jmrix.easydcc.easydccmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a EasyDccMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
  */
public class EasyDccMonAction extends AbstractAction {

    public EasyDccMonAction() {
        this("EasyDCC Command Monitor");
    }

    public EasyDccMonAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a EasyDccMonFrame
        EasyDccMonFrame f = new EasyDccMonFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("EasyDccMonAction starting EasyDccMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonAction.class);

}
