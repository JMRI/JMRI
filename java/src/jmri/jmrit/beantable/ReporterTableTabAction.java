package jmri.jmrit.beantable;

import jmri.*;

public class ReporterTableTabAction extends AbstractTableTabAction<Reporter> {

    public ReporterTableTabAction(String s) {
        super(s);
    }

    public ReporterTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected Manager<Reporter> getManager() {
        return InstanceManager.reporterManagerInstance();
    }

    @Override
    protected String getClassName() {
        return ReporterTableAction.class.getName();
    }

    @Override
    protected ReporterTableAction getNewTableAction(String choice) {
        return new ReporterTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.ReporterTable";
    }
}
