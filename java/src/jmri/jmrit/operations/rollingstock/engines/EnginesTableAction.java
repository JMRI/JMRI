package jmri.jmrit.operations.rollingstock.engines;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a EnginesTableFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2008
 */
@API(status = MAINTAINED)
public class EnginesTableAction extends AbstractAction {

    public EnginesTableAction() {
        super(Bundle.getMessage("MenuEngines")); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a engine table frame
        new EnginesTableFrame();
    }
}


