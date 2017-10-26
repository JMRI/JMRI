package jmri.jmrix.qsi;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri QSI-specific tools.
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class QSIMenu extends JMenu {

    public QSIMenu(String name, QsiSystemConnectionMemo memo) {
        this(memo);

        setText(name);
    }

    public QSIMenu(QsiSystemConnectionMemo memo) {
        super();

        if (memo != null) {
            setText(memo.getUserName());
        } else {
            setText("QSI");
        }

        if (memo != null) {
            add(new jmri.jmrix.qsi.qsimon.QsiMonAction(memo));
            add(new jmri.jmrix.qsi.packetgen.PacketGenAction(memo));
        }
    }

}
