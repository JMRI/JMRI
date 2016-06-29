package jmri.jmrix.bachrus;

import java.util.ResourceBundle;
import javax.swing.JMenu;

/**
 * Create a "Systems" menu containing the bachrus-specific tools
 *
 * @author	Andrew Crosland Copyright 2010
 */
public class SpeedoMenu extends JMenu {

    private SpeedoSystemConnectionMemo _memo = null;

    public SpeedoMenu(String name,SpeedoSystemConnectionMemo memo) {
        this(memo);
        setText(name);
    }

    public SpeedoMenu(SpeedoSystemConnectionMemo memo) {

        super();
        _memo=memo;

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle");

        // setText(rb.getString("MenuSystems"));
        setText("Speedo");

        add(new jmri.jmrix.bachrus.SpeedoConsoleAction(rb.getString("MenuItemSpeedo"),_memo));
    }

}
