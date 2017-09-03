package jmri.jmrit.withrottle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
public class ControllerFilterAction extends AbstractAction {

    public ControllerFilterAction(String name) {
        super(name);
        if ((jmri.InstanceManager.getNullableDefault(jmri.TurnoutManager.class) == null) && (jmri.InstanceManager.getNullableDefault(jmri.RouteManager.class) == null)) {
            setEnabled(false);
        }
    }

    public ControllerFilterAction() {
        this(Bundle.getMessage("MenuMenuFilter"));
    }

    public String getName() {
        return "jmri.jmrit.withrottle.ControllerFilterFrame";
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JmriJFrame frame = new ControllerFilterFrame();
        try {
            frame.initComponents();
            frame.setVisible(true);
        } catch (Exception ex) {
            log.error("Could not create Route & Turnout Filter frame");
        }

    }

    private final static Logger log = LoggerFactory.getLogger(ControllerFilterAction.class);

}
