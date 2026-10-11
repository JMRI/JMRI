package jmri.jmrit.beantable;

import jmri.*;

public class StringIOTableTabAction extends AbstractTableTabAction<StringIO> {

    public StringIOTableTabAction(String s) {
        super(s);
    }

    public StringIOTableTabAction() {
        this("Multiple Tabbed");
    }

    /** {@inheritDoc} */
    @Override
    protected Manager<StringIO> getManager() {
        return InstanceManager.getDefault(StringIOManager.class);
    }

    /** {@inheritDoc} */
    @Override
    protected String getClassName() {
        return StringIOTableAction.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    protected StringIOTableAction getNewTableAction(String choice) {
        return new StringIOTableAction(choice);
    }

    /** {@inheritDoc} */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.StringIOTable";
    }
}
