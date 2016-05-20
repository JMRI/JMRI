package jmri.jmrit.beantable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

public class SensorTableTabAction extends AbstractTableTabAction {
    
    public SensorTableTabAction(String s){
        super(s);
    }
    
    public SensorTableTabAction(){
        this("Multiple Tabbed");
    }
    
    protected Manager getManager() {
        return InstanceManager.sensorManagerInstance();
    }
       
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }
    
    protected AbstractTableAction getNewTableAction (String choice){
        return new SensorTableAction(choice);
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }
    
    static Logger log = LoggerFactory.getLogger(SensorTableTabAction.class.getName());
}
