// TurnoutTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Turnout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Swing action to create and register a
 * TurnoutTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.4 $
 */

public class TurnoutTableAction extends AbstractAction {

    public TurnoutTableAction(String s) { super(s);}
    public TurnoutTableAction() { this("Turnout Table");}

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
            public void clickOn(NamedBean t) {
                int state = ((Turnout)t).getKnownState();
                if (state==Turnout.CLOSED) ((Turnout)t).setCommandedState(Turnout.THROWN);
                else ((Turnout)t).setCommandedState(Turnout.CLOSED);
            }
            public JButton configureButton() {
                return new JButton("Thrown");
            }
        };
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m);
        f.setTitle("Turnout Table");
        f.show();
    }
}


/* @(#)TurnoutTableAction.java */
