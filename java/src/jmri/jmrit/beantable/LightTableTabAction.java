package jmri.jmrit.beantable;

import jmri.InstanceManager;
import jmri.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightTableTabAction extends AbstractTableTabAction {

    /**
     *
     */
    private static final long serialVersionUID = 2814960247992024318L;

    public LightTableTabAction(String s) {
        super(s);
    }

    public LightTableTabAction() {
        this("Multiple Tabbed");
    }

    protected Manager getManager() {
        return InstanceManager.lightManagerInstance();
    }

    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    protected AbstractTableAction getNewTableAction(String choice) {
        return new LightTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    private final static Logger log = LoggerFactory.getLogger(LightTableTabAction.class.getName());
}
