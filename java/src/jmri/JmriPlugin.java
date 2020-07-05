package jmri;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Method for invoking user code at startup time.
 * <p>
 * This class provides a null static member. By replacing it with another
 * implementation, the user can update configuration, etc at startup time.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2012
 */
@API(status = STABLE)
public class JmriPlugin {

    public static void start(JFrame mainFrame, JMenuBar menuBar) {

        // Example: Add a new menu with one item
        //javax.swing.JMenu menu = new javax.swing.JMenu("Sample");
        //javax.swing.JMenuItem item = new javax.swing.JMenuItem("Item");
        //item.addActionListener(new java.awt.event.ActionListener() {
        //    public void actionPerformed(java.awt.event.ActionEvent e) {
        //        System.out.println("Selected");
        //    }
        //});
        //menu.add(item);
        //menuBar.add(menu);
    }
}
