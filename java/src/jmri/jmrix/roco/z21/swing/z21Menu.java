// z21Menu.java

package jmri.jmrix.roco.z21.swing;

import java.util.ResourceBundle;

import javax.swing.JMenu;

/**
 * Create a menu containing the Z21 specific tools
 *
 * @author	Paul Bender   Copyright 2014 
 * @version     $Revision$
 */
public class z21Menu extends JMenu {
    public z21Menu(String name,jmri.jmrix.roco.z21.z21SystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public z21Menu(jmri.jmrix.roco.z21.z21SystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");

        if(memo != null)
          setText(memo.getUserName());
        else
          setText(rb.getString("Menuz21"));

        add(new jmri.jmrix.roco.z21.swing.mon.z21MonAction(rb.getString("jmri.jmrix.roco.z21.swing.mon.z21MonAction"),memo));
        add(new jmri.jmrix.roco.z21.swing.packetgen.PacketGenAction(rb.getString("jmri.jmrix.roco.z21.swing.packetgen.PacketGenAction"),memo));
    }

}


