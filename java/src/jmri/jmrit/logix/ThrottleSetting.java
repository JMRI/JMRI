package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThrottleSetting {

    private long _time;
    private String _command;
    private String _value;
    @SuppressWarnings("rawtypes") // _namedHandle may be of 3 different types
    private NamedBeanHandle _namedHandle = null;

    public ThrottleSetting() {
    }

    public ThrottleSetting(long time, String command, String value, String beanName) {
        _time = time;
        _command = command;
        _value = value;
        setNamedBean(command, beanName); 
    }

    public ThrottleSetting(ThrottleSetting ts) {
        _time = ts.getTime();
        _command = ts.getCommand();
        _value = ts.getValue();
        _namedHandle = ts.getNamedBeanHandle();
    }

    /**
     * Time is an object so that a "synch to block entry" notation can be used
     * rather than elapsed time.
     *
     * @param time the time in some unit
     */
    public void setTime(long time) {
        _time = time;
    }

    public long getTime() {
        return _time;
    }

    public void setCommand(String command) {
        _command = command;
    }

    public String getCommand() {
        return _command;
    }

    public void setValue(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    //_namedHandle may be of 3 different types
    public void setNamedBean(String cmd, String name) {
        if (log.isDebugEnabled()) {
            log.debug("setNamedBean({}, {})", cmd, name);
        }
        if (cmd == null || name == null || name.trim() == "") {
            _namedHandle = null;
            return;
        }
        cmd = cmd.toUpperCase();
        try {
            if ("SET SENSOR".equals(cmd) || "WAIT SENSOR".equals(cmd)) {
                Sensor s = InstanceManager.sensorManagerInstance().provideSensor(name);
                _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, s);            
            } else if ("RUN WARRANT".equals(cmd)) {
                Warrant w = InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).provideWarrant(name);
                _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, w);                        
            } else {
                OBlock b = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).provideOBlock(name);
                _namedHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, b);            
            }            
        } catch (IllegalArgumentException iae) {
            log.error(iae.toString());
        }
    }
  
    @SuppressWarnings("unchecked") // _namedHandle may be of 3 different types
    public <T extends NamedBean> void setNamedBeanHandle(NamedBeanHandle <T> bh) {
        _namedHandle = bh;
    }
    
    @SuppressWarnings("unchecked") // _namedHandle may be of 3 different types
    public <T extends NamedBean> NamedBeanHandle <T> getNamedBeanHandle() {
        return _namedHandle;
    }

    public String getBeanDisplayName() {
        if (_namedHandle == null) {
            return null;
        }
        return _namedHandle.getBean().getDisplayName();
    }

    public String getBeanSystemName() {
        return _namedHandle.getBean().getSystemName();
    }

    @Override
    public String toString() {
        return "ThrottleSetting: wait " + _time + "ms then do " + _command + " = " + _value + " for bean " + getBeanDisplayName();
    }
    
    private final static Logger log = LoggerFactory.getLogger(ThrottleSetting.class.getName());
}
