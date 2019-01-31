package jmri.jmrit.beantable;

import jmri.*;

public class IdTagTableTabAction extends AbstractTableTabAction<IdTag> {

    public IdTagTableTabAction(String s) {
        super(s);
    }

    public IdTagTableTabAction() {
        this("Multiple Tabbed");
    }

    /** {@inheritDoc} */
    @Override
    protected Manager<IdTag> getManager() {
        return InstanceManager.getDefault(IdTagManager.class);
    }

    /** {@inheritDoc} */
    @Override
    protected String getClassName() {
        return IdTagTableAction.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    protected IdTagTableAction getNewTableAction(String choice) {
        return new IdTagTableAction(choice);
    }

    /** {@inheritDoc} */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.IdTagTable";
    }
}
