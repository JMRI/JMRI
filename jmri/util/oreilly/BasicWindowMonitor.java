
/*   BasicWindowMonitor.java
 *           cribbed from the O'Reilly Swing book chapter 2
 *
 */

package jmri.util.oreilly;

import java.awt.event.*;
import java.awt.Window;

public class BasicWindowMonitor extends WindowAdapter {

  public void windowClosing(WindowEvent e) {
    Window w = e.getWindow();
    w.setVisible(false);
    w.dispose();
	BasicQuit.handleQuit();
  }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(BasicWindowMonitor.class.getName());
}
