package jmri.jmrit.simplelightctrl;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a SimpleTurnoutCtrlFrame
 * object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = MAINTAINED)
public class SimpleLightCtrlAction extends AbstractAction {

    public SimpleLightCtrlAction(String s) {
        super(s);

        // disable ourself if there is no primary light manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.LightManager.class) == null) {
            setEnabled(false);
        }
    }

    public SimpleLightCtrlAction() {
        this(Bundle.getMessage("Lights"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        SimpleLightCtrlFrame f = new SimpleLightCtrlFrame();
        f.setVisible(true);
    }

}
