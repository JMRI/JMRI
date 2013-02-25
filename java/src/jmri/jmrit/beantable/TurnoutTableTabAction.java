package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

public class TurnoutTableTabAction extends AbstractTableTabAction {
    
    public TurnoutTableTabAction(String s){
        super(s);
    }
    
    public TurnoutTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.turnoutManagerInstance();
    }
    
    protected String getClassName() {
        return TurnoutTableAction.class.getName();
    }
       
    protected AbstractTableAction getNewTableAction (String choice){
        return new TurnoutTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.TurnoutTable";
    }
    
    static Logger log = LoggerFactory.getLogger(TurnoutTableTabAction.class.getName());
}
