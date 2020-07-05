package jmri.jmrit.sensorgroup;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a SensorGroupFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2007
 */
@API(status = MAINTAINED)
public class SensorGroupAction extends AbstractAction {

    public SensorGroupAction(String s) {
        super(s);

        // disable ourself if there is no route manager object available
        if (jmri.InstanceManager.getNullableDefault(jmri.RouteManager.class) == null) {
            setEnabled(false);
        }
    }

    public SensorGroupAction() {
        this("Define Sensor Group...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SensorGroupFrame f = new SensorGroupFrame();
        f.initComponents();
        f.setVisible(true);
    }
}
