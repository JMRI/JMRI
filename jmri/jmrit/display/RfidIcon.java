package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.catalog.NamedIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.*;
import javax.swing.JPopupMenu;

/**
 * An icon to display info from RFID reader.<P>
 *
 * @author Bob Jacobsen  Copyright (c) 2004
 * @version $Revision: 1.1 $
 */

public class RfidIcon extends PositionableLabel {

    public RfidIcon() {
        // super ctor call to make sure this is an icon label
        super("-------");
        System.out.println("rfid ctor");
        setText("+++++++");
    }

    public void setProperToolTip() {
        setToolTipText("RFID reader");
    }

    protected int maxHeight() {
        return (new JLabel(this.getText())).getHeight();
    }
    protected int maxWidth() {
        return (new JLabel(this.getText())).getWidth();
    }


	// add code here to display state when something happens

    public void dispose() {
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RfidIcon.class.getName());
}
