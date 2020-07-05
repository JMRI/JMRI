package jmri.jmrit.automat.monitor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a AutomatTable GUI
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2008
 */
@API(status = MAINTAINED)
public class AutomatTableAction extends AbstractAction {

    public AutomatTableAction(String actionName) {
        super(actionName);
    }

    public AutomatTableAction() {
        this(Bundle.getMessage("TitleAutomatTable"));
    }

    AutomatTableDataModel m;

    AutomatTableFrame f;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create the frame
        f = new AutomatTableFrame(new AutomatTableDataModel());
        f.pack();
        f.setVisible(true);
    }

}
