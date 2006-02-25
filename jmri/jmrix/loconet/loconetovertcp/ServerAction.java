package jmri.jmrix.loconet.loconetovertcp;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author      Alex Shepherd Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.LnTrafficController;

public class ServerAction
  extends AbstractAction {

  public ServerAction(String s) {
    super(s);
  }

  public ServerAction() {
    this("LocoNetOverTcp Server");
  }

  public void actionPerformed(ActionEvent e) {
    ServerFrame f = ServerFrame.getInstance();
    f.show();
  }
}
