
/*   BasicWindowMonitor.java
 *           cribbed from the O'Reilly Swing book chapter 2
 *
 */


import java.awt.event.*;
import java.awt.Window;

public class BasicWindowMonitor extends WindowAdapter {

  public void windowClosing(WindowEvent e) {
    Window w = e.getWindow();
    w.setVisible(false);
    w.dispose();
    System.exit(0);
  }
}
