package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

public class LightTableTabAction extends AbstractTableTabAction {

    public LightTableTabAction(String s){
        super(s);
    }
    
    public LightTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.lightManagerInstance();
    }
    
    protected String getClassName() {
        return LightTableAction.class.getName();
    }
    
    protected AbstractTableAction getNewTableAction (String choice){
        return new LightTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }
    
    static Logger log = LoggerFactory.getLogger(LightTableTabAction.class.getName());
}
