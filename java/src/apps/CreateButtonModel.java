package apps;

import apps.gui3.Apps3;
import apps.startup.AbstractActionModel;
import javax.swing.Action;
import javax.swing.JButton;
import jmri.JmriException;

/**
 * Creates a button when the program is started.
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
 * @see apps.startup.CreateButtonModelFactory
 */
public class CreateButtonModel extends AbstractActionModel {

    // private final static Logger log = LoggerFactory.getLogger(CreateButtonModel.class);

    public CreateButtonModel() {
        super();
    }

    @Override
    protected void performAction(Action action) throws JmriException {
        JButton b = new JButton(action);
        b.setToolTipText(this.toString());
        if (Apps.buttonSpace() != null) {
            Apps.buttonSpace().add(b);
        } else if (Apps3.buttonSpace() != null) {
            Apps3.buttonSpace().add(b);
        }
    }
}
