package jmri.jmrix.loconet.cmdstnconfig;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;
import java.util.ResourceBundle;

/**
 * Create and register a CmdStnConfigFrame object.
 *
 * @author			Alex Shepherd    Copyright (C) 2004
 * @version			$Revision: 1.1 $
 */

public class CmdStnConfigAction extends AbstractAction {

    public CmdStnConfigAction(String s) {
        super(s);
    }

    public CmdStnConfigAction() {
        this(ResourceBundle.getBundle("jmri.jmrix.loconet.LocoNetBundle").getString("MenuItemCmdStnConfig"));
    }

    public void actionPerformed(ActionEvent e) {
        CmdStnConfigFrame f = new CmdStnConfigFrame();
        f.initComponents();

          // connect to the LnTrafficController
        f.connect(LnTrafficController.instance());

          // make visible
        f.show();
    }
}