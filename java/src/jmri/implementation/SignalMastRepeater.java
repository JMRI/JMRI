// SignalMastRepeater.java

package jmri.implementation;

 /**
 * A simple class that repeaters the state of one SignalMast 
 * to another
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 *
 * @version     $Revision$
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import jmri.SignalMast;
import jmri.NamedBeanHandle;

public class SignalMastRepeater {

    public final static int BOTHWAY = 0x00;
    public final static int MASTERTOSLAVE = 0x01;
    public final static int SLAVETOMASTER = 0x02;

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    
    protected NamedBeanHandle<SignalMast> _master;
    protected NamedBeanHandle<SignalMast> _slave;
    boolean _enabled = true;
    int _direction = BOTHWAY;
    
    public SignalMastRepeater(SignalMast master, SignalMast slave){
        _master = nbhm.getNamedBeanHandle(master.getDisplayName(), master);
        _slave = nbhm.getNamedBeanHandle(slave.getDisplayName(), slave);
    }
    
    public SignalMastRepeater(String master, String slave){
        SignalMast masterMast = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(master);
        _master = nbhm.getNamedBeanHandle(master, masterMast);
        SignalMast slaveMast = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(slave);
        _slave = nbhm.getNamedBeanHandle(slave, slaveMast);
    }
    
    public SignalMast getMasterMast(){
        return _master.getBean();
    }
    
    public SignalMast getSlaveMast(){
        return _slave.getBean();
    }
    
    public String getMasterMastName(){
        return _master.getName();
    }
    
    public String getSlaveMastName(){
        return _slave.getName();
    }
    
    public int getDirection(){
        return _direction;
    }
    
    public void setDirection(int dir){
        if(dir==_direction)
            return;
        _direction = dir;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        initialise();
    }
    
    public void setEnabled(boolean en){
        if(_enabled == en){
            return;
        }
        _enabled = en;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        initialise();
    }
    
    public boolean getEnabled(){
        return _enabled;
    }
    
    public void initialise(){
        if(disposed){
            log.error("Trying to initialise a repeater that has already been disposed");
        }
        if(!_enabled)
            return;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        switch(_direction){
            case MASTERTOSLAVE : getMasterMast().addPropertyChangeListener(mastListener);
                                 updateStatus(getMasterMast(), getSlaveMast());
                                 break;
            case SLAVETOMASTER : getSlaveMast().addPropertyChangeListener(mastListener);
                                 updateStatus(getSlaveMast(), getMasterMast());
                                 break;
            default : getMasterMast().addPropertyChangeListener(mastListener);
                      getSlaveMast().addPropertyChangeListener(mastListener);
                      updateStatus(getMasterMast(), getSlaveMast());
        }
    }
    
    PropertyChangeListener mastListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
            if(disposed)
                return;
            if(e.getSource()==getMasterMast())
                updateStatus(getMasterMast(), getSlaveMast());
            else
                updateStatus(getSlaveMast(), getMasterMast());
        }
    };
    
    void updateStatus(SignalMast mastFrom, SignalMast mastTo){
        if(log.isDebugEnabled()) log.debug("Updating from mast " + mastFrom.getDisplayName() + ":" + mastFrom.getAspect() + " to mast " + mastTo.getDisplayName());
        if(mastFrom.getAspect()!=null)
            mastTo.setAspect(mastFrom.getAspect());
    }
    
    boolean disposed = false;
    
    public void dispose(){
        disposed = true;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        _master = null;
        _slave = null;
    }
    
    static Logger log = LoggerFactory.getLogger(SignalMastRepeater.class.getName());

}
