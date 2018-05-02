package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.annotation.Nonnull;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignalMastRepeater {

    public final static int BOTHWAY = 0x00;
    public final static int MASTERTOSLAVE = 0x01;
    public final static int SLAVETOMASTER = 0x02;

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    protected NamedBeanHandle<SignalMast> _master;
    protected NamedBeanHandle<SignalMast> _slave;
    boolean _enabled = true;
    int _direction = BOTHWAY;

    public SignalMastRepeater(@Nonnull SignalMast master, @Nonnull SignalMast slave) {
        _master = nbhm.getNamedBeanHandle(master.getDisplayName(), master);
        _slave = nbhm.getNamedBeanHandle(slave.getDisplayName(), slave);
    }

    public SignalMastRepeater(@Nonnull String master, @Nonnull String slave) {
        SignalMast masterMast = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(master);
        if (masterMast == null) throw new IllegalArgumentException("master mast must exist, \""+master+"\" doesn't");
        _master = nbhm.getNamedBeanHandle(master, masterMast);
        SignalMast slaveMast = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(slave);
        if (slaveMast == null) throw new IllegalArgumentException("slave mast must exist, \""+slave+"\" doesn't");
        _slave = nbhm.getNamedBeanHandle(slave, slaveMast);
    }

    @Nonnull 
    public SignalMast getMasterMast() {
        return _master.getBean();
    }

    @Nonnull 
    public SignalMast getSlaveMast() {
        return _slave.getBean();
    }

    @Nonnull 
    public String getMasterMastName() {
        return _master.getName();
    }

    @Nonnull 
    public String getSlaveMastName() {
        return _slave.getName();
    }

    public int getDirection() {
        return _direction;
    }

    public void setDirection(int dir) {
        if (dir == _direction) {
            return;
        }
        _direction = dir;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        initialise();
    }

    public void setEnabled(boolean en) {
        if (_enabled == en) {
            return;
        }
        _enabled = en;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        initialise();
    }

    public boolean getEnabled() {
        return _enabled;
    }

    public void initialise() {
        if (disposed) {
            log.error("Trying to initialise a repeater that has already been disposed");
        }
        if (!_enabled) {
            return;
        }
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        switch (_direction) {
            case MASTERTOSLAVE:
                getMasterMast().addPropertyChangeListener(mastListener);
                updateStatus(getMasterMast(), getSlaveMast());
                break;
            case SLAVETOMASTER:
                getSlaveMast().addPropertyChangeListener(mastListener);
                updateStatus(getSlaveMast(), getMasterMast());
                break;
            default:
                getMasterMast().addPropertyChangeListener(mastListener);
                getSlaveMast().addPropertyChangeListener(mastListener);
                updateStatus(getMasterMast(), getSlaveMast());
        }
    }

    PropertyChangeListener mastListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (disposed) {
                return;
            }
            if (e.getSource() == getMasterMast()) {
                updateStatus(getMasterMast(), getSlaveMast());
            } else {
                updateStatus(getSlaveMast(), getMasterMast());
            }
        }
    };

    void updateStatus(@Nonnull SignalMast mastFrom, @Nonnull SignalMast mastTo) {
        if (log.isDebugEnabled()) {
            log.debug("Updating from mast " + mastFrom.getDisplayName() + ":" + mastFrom.getAspect() + " to mast " + mastTo.getDisplayName());
        }
        if (mastFrom.getAspect() != null) {
            mastTo.setAspect(mastFrom.getAspect());
        }
    }

    boolean disposed = false;

    public void dispose() {
        disposed = true;
        getMasterMast().removePropertyChangeListener(mastListener);
        getSlaveMast().removePropertyChangeListener(mastListener);
        _master = null;
        _slave = null;
    }

    private final static Logger log = LoggerFactory.getLogger(SignalMastRepeater.class);

}
