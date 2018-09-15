package jmri.jmrix.roco.z21.swing;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.JMenu;


/**
 * Create a menu containing the Z21 specific tools
 *
 * @author	Paul Bender Copyright 2014
 */
public class Z21Menu extends JMenu {

    public Z21Menu(String name, jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public Z21Menu(@Nonnull jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.roco.z21.z21ActionListBundle");

        setText(memo.getUserName());

        add(new jmri.jmrix.roco.z21.swing.mon.Z21MonAction());
        add(new jmri.jmrix.roco.z21.swing.packetgen.PacketGenAction(rb.getString("jmri.jmrix.roco.z21.swing.packetgen.PacketGenAction"), memo));
        add(new jmri.jmrix.roco.z21.swing.configtool.Z21ConfigAction(rb.getString("jmri.jmrix.roco.z21.swing.configtool.Z21ConfigAction"), memo));
    }

}
