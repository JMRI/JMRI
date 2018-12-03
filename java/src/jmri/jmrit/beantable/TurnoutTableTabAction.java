package jmri.jmrit.beantable;

import jmri.*;

public class TurnoutTableTabAction extends AbstractTableTabAction<Turnout> {

    public TurnoutTableTabAction(String s) {
        super(s);
    }

    public TurnoutTableTabAction() {
        this("Multiple Tabbed");
    }

    @Override
    protected Manager<Turnout> getManager() {
        return InstanceManager.turnoutManagerInstance();
    }

    @Override
    protected String getClassName() {
        return TurnoutTableAction.class.getName();
    }

    @Override
    protected TurnoutTableAction getNewTableAction(String choice) {
        return new TurnoutTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }
}
