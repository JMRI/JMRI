package jmri.jmrix.qsi;

import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the Jmri QSI-specific tools
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class QSIMenu extends JMenu {

    private QsiSystemConnectionMemo _memo = null;

    public QSIMenu(String name,QsiSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public QSIMenu(QsiSystemConnectionMemo memo) {

        super();

        //ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");
        // setText(rb.getString("MenuSystems"));
        setText("QSI");
        _memo = memo;

        add(new jmri.jmrix.qsi.qsimon.QsiMonAction(_memo));
        add(new jmri.jmrix.qsi.packetgen.PacketGenAction(_memo));


        setText(memo.getUserName());
    }

}
