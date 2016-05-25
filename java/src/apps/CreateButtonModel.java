package apps;

/**
 * Creates a button when the program is started.
 * <P>
 * The list of actions available is defined in the {@link AbstractActionModel}
 * superclass.
 * <P>
 * This is a separate class, even though it has no additional behavior, so that
 * persistence systems realize the type of data being stored.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @see apps.startup.CreateButtonModelFactory
 */
public class CreateButtonModel extends AbstractActionModel {

    public CreateButtonModel() {
        super();
    }
}
