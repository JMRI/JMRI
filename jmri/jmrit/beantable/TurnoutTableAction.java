// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.*;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import jmri.jmrix.loconet.SlotManager;

/**
 * Swing action to create and register a
 * TurnoutTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class TurnoutTableAction extends AbstractAction {

    public TurnoutTableAction(String s) { super(s);}
    public TurnoutTableAction() { this("TurnoutTable");}

    public void actionPerformed(ActionEvent e) {

        // create the model, with modifications for Turnouts
        BeanTableDataModel m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.turnoutManagerInstance().getBySystemName(name).getKnownState();
                switch (val) {
                case Turnout.CLOSED: return "Closed";
                case Turnout.THROWN: return "Thrown";
                case Turnout.UNKNOWN: return "Unknown";
                case Turnout.INCONSISTENT: return "Inconsistent";
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.turnoutManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.turnoutManagerInstance().getBySystemName(name);}
        };
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m);
        f.show();
    }
}


/* @(#)SlotMonAction.java */
