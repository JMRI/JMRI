// ServerAction.java

package jmri.jmrix.loconet.loconetovertcp;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author      Alex Shepherd Copyright (C) 2006
 * @version	$Revision$
 */

public class ServerAction
  extends AbstractAction {

  public ServerAction(String s) {
    super(s);
      // Get a server instance to cause the config to be read and the server
      // started if necessary
    Server.getInstance();
  }

  public ServerAction() {
    this("LocoNetOverTcp Server");
      // Get a server instance to cause the config to be read and the server
      // started if necessary
    Server.getInstance();
  }

  public void actionPerformed(ActionEvent e) {
    ServerFrame f = ServerFrame.getInstance();
    f.setVisible(true);
  }
}
