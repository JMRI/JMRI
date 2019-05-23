package apps;

import apps.startup.AbstractActionModel;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.JmriException;

/**
 * Invokes a Swing Action when the program is started.
 * <p>
 * The list of actions available is defined in the {@link AbstractActionModel}
 * superclass.
 * <p>
 * This is a separate class, even though it has no additional behavior, so that
 * persistence systems realize the type of data being stored.
 * <p>
 * This class remains in the {@code apps} package for historical reasons related
 * to preferences storage.
 *
 * @author Bob Jacobsen Copyright 2003
 * @see apps.startup.PerformActionModelFactory
 */
public class PerformActionModel extends AbstractActionModel {

    public PerformActionModel() {
        super();
    }

    @Override
    protected void performAction(Action action) throws JmriException {
        action.actionPerformed(new ActionEvent("prefs", 0, ""));
    }
}
