// AbstractTurnout.java

package jmri;

/**
 * Abstract base for the Turnout interface.
 * <P>
 * Implements NONE feedback, where
 * the KnownState and CommandedState track each other. If you want to
 * implement some other feedback, override and modify setCommandedState()
 * here.
 * <P>
 * Implements the parameter binding support.
 * <P>
 * Note that we consider it an error for there to be more than one object
 * that corresponds to a particular physical turnout on the layout.
 *
 * Description:		Abstract class providing the basic logic of the Turnout interface
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.13 $
 */
public abstract class AbstractTurnout extends AbstractNamedBean 
        implements Turnout, java.io.Serializable, java.beans.PropertyChangeListener {

    public AbstractTurnout(String systemName) {
        super(systemName);
    }

    public AbstractTurnout(String systemName, String userName) {
        super(systemName, userName);
    }

    /**
     * Handle a request to change state, typically by
     * sending a message to the layout in some child class.
     * @param s new state value
     */
    abstract protected void forwardCommandChangeToLayout(int s);

    // implementing classes will typically have a function/listener to get
    // updates from the layout, which will then call
    //		public void firePropertyChange(String propertyName,
    //					       	Object oldValue,
    //						Object newValue)
    // _once_ if anything has changed state

    /**
     * Sets a new Commanded state, if need be notifying the
     * listeners, but does NOT send the command downstream.  This is used
     * when a new commanded state is noticed from another command.
     */
    public void newCommandedState(int s) {
        if (_commandedState != s) {
            int oldState = _commandedState;
            _commandedState = s;
            firePropertyChange("CommandedState", new Integer(oldState),
                               new Integer(_commandedState));
        }
    }

    public int getKnownState() {return _knownState;}

    public void setCommandedState(int s) {
        forwardCommandChangeToLayout(s);
        newCommandedState(s);
        // optionally handle feedback
        if (_activeFeedbackType == DIRECT) newKnownState(s); 
    }

    public int getCommandedState() {return _commandedState;}

    /**
     * Add a protected newKnownState() for use by implementations. Not intended for
     * general use, e.g. for users to set the KnownState, but rather for
     * the code to have a way to set the state and fire notifications.
     */
    protected void newKnownState(int s) {
        if (_knownState != s) {
            int oldState = _knownState;
            _knownState = s;
            firePropertyChange("KnownState", new Integer(oldState), new Integer(_knownState));
        }
    }

    /**
     * Implement a shorter name for setCommandedState.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * setCommandedState instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public void setState(int s) { setCommandedState(s); }
    
    /**
     * Implement a shorter name for getCommandedState.
     *<P>
     * This generally shouldn't be used by Java code; use 
     * getCommandedState instead.  The is provided to make Jython
     * script access easier to read.  
     */
    public int getState() { return getCommandedState(); }
    
    protected String[] _validFeedbackNames = {"DIRECT", "ONESENSOR", "TWOSENSOR"};
    protected int[] _validFeedbackModes = {DIRECT, ONESENSOR, TWOSENSOR};
    protected int _validFeedbackTypes = DIRECT | ONESENSOR | TWOSENSOR;
    
    protected int _activeFeedbackType   = DIRECT;

    private int _knownState     = UNKNOWN;
    private int _commandedState = UNKNOWN;

    public int getValidFeedbackTypes() {return _validFeedbackTypes;}

    public String[] getValidFeedbackNames() {
        return _validFeedbackNames;
    }
    
    public void setFeedbackMode(String mode) {
        for (int i = 0; i<_validFeedbackNames.length; i++) {
            if (mode.equals(_validFeedbackNames[i])) {
                setFeedbackMode(_validFeedbackModes[i]);
                return;
            }
        }
        throw new IllegalArgumentException("Unexpected mode: "+mode);
    }
    
    public void setFeedbackMode(int mode) {
        // check for error - following removed the low bit from mode
        int test = mode & (mode-1);
        if (test!=0) throw new IllegalArgumentException("More than one bit set: "+mode);
        // set the value
        _activeFeedbackType = mode;
    }

    public int getFeedbackMode() { return _activeFeedbackType; }

    public String getFeedbackModeName() {
        for (int i = 0; i<_validFeedbackNames.length; i++) {
            if (_activeFeedbackType==_validFeedbackModes[i]) {
                return _validFeedbackNames[i];
            }
        }
        throw new IllegalArgumentException("Unexpected internal mode: "+_activeFeedbackType);
    }

    Sensor _firstSensor = null;
    Sensor _secondSensor = null;
    public void provideFirstFeedbackSensor(Sensor s) {
        // if need be, clean listener
        if (_firstSensor!=null) {
            _firstSensor.removePropertyChangeListener(this);
        }

        _firstSensor = s;
        
        // if need be, set listener
        if (_firstSensor!=null) {
            _firstSensor.addPropertyChangeListener(this);
        }
    }

    public void provideSecondFeedbackSensor(Sensor s) {
        // if need be, clean listener
        if (_secondSensor!=null) {
            _secondSensor.removePropertyChangeListener(this);
        }

        _secondSensor = s;

        // if need be, set listener
        if (_secondSensor!=null) {
            _secondSensor.addPropertyChangeListener(this);
        }
    }

    public Sensor getFirstSensor() { return _firstSensor; }
    public Sensor getSecondSensor()  { return _secondSensor; }
    
    public void setInitialKnownStateFromFeedback() {
        if (_activeFeedbackType==ONESENSOR) {
			// ONESENSOR feedback 
			if (_firstSensor!=null) {
				// set according to state of sensor
				int sState = _firstSensor.getKnownState();
				if (sState==Sensor.ACTIVE) 
					newKnownState(THROWN);
				else if (sState==Sensor.INACTIVE)
					newKnownState(CLOSED);
			}
			else {
				log.warn("expected Sensor 1 not defined - "+
					getSystemName());
				newKnownState(UNKNOWN);
			}
		}
		else if (_activeFeedbackType==TWOSENSOR) {
			// TWOSENSOR feedback
			int s1State = Sensor.UNKNOWN;
			int s2State = Sensor.UNKNOWN;
			if (_firstSensor!=null)
				s1State = _firstSensor.getKnownState();
			else {
				log.warn("expected Sensor 1 not defined - "+
					getSystemName());
			}
			if (_secondSensor!=null)
				s2State = _secondSensor.getKnownState();
			else {
				log.warn("expected Sensor 2 not defined - "+
					getSystemName());
			}
			// set Turnout state according to sensors
			if ((s1State==Sensor.ACTIVE) && (s2State==Sensor.INACTIVE))
				newKnownState(THROWN);
			else if ((s1State==Sensor.INACTIVE) && (s2State==Sensor.ACTIVE))
				newKnownState(CLOSED);
			else if (_knownState!=UNKNOWN)
				newKnownState(UNKNOWN);
		}
		else {
			// nothing required at this time for other modes
		}
	}

    /**
     * React to sensor changes by changing the KnownState
     * if using an appropriate sensor mode
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        // top level, find the mode
        if (_activeFeedbackType == ONESENSOR) {
            // check for match
            if (evt.getSource()==_firstSensor) {
                // check change type
                if (!evt.getPropertyName().equals("KnownState")) return;
                // OK, now have to handle it
                int mode = ((Integer)evt.getNewValue()).intValue();
                if (mode==Sensor.ACTIVE)
                    newKnownState(THROWN);
                else if (mode==Sensor.INACTIVE)
                    newKnownState(CLOSED);
            } else {
                // unexected mismatch
                log.warn("expected sensor "+_firstSensor.getSystemName()
                        +" was "+((Sensor)evt.getSource()).getSystemName());
            }
            // end ONESENSOR block
        } else if (_activeFeedbackType == TWOSENSOR) {
            // check change type
            if (!evt.getPropertyName().equals("KnownState")) return;
            // OK, now have to handle it
            int mode = ((Integer)evt.getNewValue()).intValue();
            Sensor s = (Sensor)evt.getSource();
            if ( (mode==Sensor.ACTIVE) && (s==_secondSensor))
                newKnownState(CLOSED);
            else if ( (mode==Sensor.ACTIVE) && (s==_firstSensor))
                newKnownState(THROWN);
            // end TWOSENSOR block
        } else
            // don't need to do anything
            return;
    }
    
    public void dispose() {
        if (_firstSensor!=null) {
            _firstSensor.removePropertyChangeListener(this);
        }
        _firstSensor = null;
        if (_secondSensor!=null) {
            _secondSensor.removePropertyChangeListener(this);
        }
        _secondSensor = null;
    }
 }

/* @(#)AbstractTurnout.java */
