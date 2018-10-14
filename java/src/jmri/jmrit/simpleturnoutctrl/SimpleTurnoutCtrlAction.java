package jmri.jmrit.simpleturnoutctrl;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Swing action to create and register a SimpleTurnoutCtrlFrame
 * object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SimpleTurnoutCtrlAction extends JmriAbstractAction {

    public SimpleTurnoutCtrlAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public SimpleTurnoutCtrlAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public SimpleTurnoutCtrlAction(String s) {
        super(s);

        // disable ourself if there is no primary turnout manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.TurnoutManager.class) == null) {
            setEnabled(false);
        }
    }

    public SimpleTurnoutCtrlAction() {
        this(Bundle.getMessage("Turnouts"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SimpleTurnoutCtrlFrame f = new SimpleTurnoutCtrlFrame();
        f.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
