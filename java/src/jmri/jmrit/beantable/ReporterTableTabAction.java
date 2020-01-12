package jmri.jmrit.beantable;

import jmri.*;

public class ReporterTableTabAction extends AbstractTableTabAction<Reporter> {

    public ReporterTableTabAction(String s) {
        super(s);
    }

    public ReporterTableTabAction() {
        this("Multiple Tabbed");
    }

    /** {@inheritDoc} */
    @Override
    protected Manager<Reporter> getManager() {
        return InstanceManager.getDefault(ReporterManager.class);
    }

    /** {@inheritDoc} */
    @Override
    protected String getClassName() {
        return ReporterTableAction.class.getName();
    }

    /** {@inheritDoc} */
    @Override
    protected ReporterTableAction getNewTableAction(String choice) {
        return new ReporterTableAction(choice);
    }

    /** {@inheritDoc} */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.ReporterTable";
    }
}
