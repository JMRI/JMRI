package apps;

import apps.startup.AbstractActionModel;

/**
 * Creates a button when the program is started.
 * <p>
 * The list of actions available is defined in the {@link AbstractActionModel}
 * superclass.</p>
 * <p>
 * This is a separate class, even though it has no additional behavior, so that
 * persistence systems realize the type of data being stored.</p>
 * <p>
 * This class remains in the {@code apps} package for historical reasons related
 * to preferences storage.</p>
 *
 * @author	Bob Jacobsen Copyright 2003
 * @see apps.startup.CreateButtonModelFactory
 */
public class CreateButtonModel extends AbstractActionModel {

    public CreateButtonModel() {
        super();
    }
}
