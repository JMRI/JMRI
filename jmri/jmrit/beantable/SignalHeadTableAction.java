// SignalHeadTableAction.java

package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

/**
 * Swing action to create and register a
 * SignalHeadTable GUI
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */

public class SignalHeadTableAction extends AbstractAction {

    public SignalHeadTableAction(String s) { super(s);}
    public SignalHeadTableAction() { this("SignalHead Table");}

    public void actionPerformed(ActionEvent e) {

        // create the model, with modifications for SignalHeads
        BeanTableDataModel m = new BeanTableDataModel() {
            public String getValue(String name) {
                int val = InstanceManager.signalHeadManagerInstance().getBySystemName(name).getAppearance();
                switch (val) {
                case SignalHead.RED: return "Red";
                case SignalHead.YELLOW: return "Yellow";
                case SignalHead.GREEN: return "Green";
                case SignalHead.FLASHRED: return "Flashing Red";
                case SignalHead.FLASHYELLOW: return "Flashing Yellow";
                case SignalHead.FLASHGREEN: return "Flashing Green";
                case SignalHead.DARK: return "Dark";
                default: return "Unexpected value: "+val;
                }
            }
            public Manager getManager() { return InstanceManager.signalHeadManagerInstance(); }
            public NamedBean getBySystemName(String name) { return InstanceManager.signalHeadManagerInstance().getBySystemName(name);}
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead)t).getAppearance();
                int newState;
                switch (oldState) {
                case SignalHead.RED: newState = SignalHead.YELLOW; break;
                case SignalHead.YELLOW: newState = SignalHead.GREEN; break;
                case SignalHead.GREEN: newState = SignalHead.DARK; break;
                case SignalHead.FLASHRED: newState = SignalHead.DARK; break;
                case SignalHead.FLASHYELLOW: newState = SignalHead.DARK; break;
                case SignalHead.FLASHGREEN: newState = SignalHead.DARK; break;
                case SignalHead.DARK: newState = SignalHead.RED; break;
                default: newState = SignalHead.DARK; log.warn("Unexpected state "+oldState+" becomes DARK");break;
                }
               ((SignalHead)t).setAppearance(newState);
            }
            public JButton configureButton() {
                return new JButton("Yellow");
            }
        };
        // create the frame
        BeanTableFrame f = new BeanTableFrame(m);
        f.setTitle("SignalHead Table");
        f.show();
    }
}


/* @(#)SignalHeadTableAction.java */
