package jmri.jmrit.ctc;

import java.beans.PropertyChangeListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */

/*
    Presently (as of 11/14/2018), this class just supports the two features
    that are required of the higher level system relating to the CTC system:
1.) Held.  JMRI Term.  I believe the proper term is "proceed" on the CTC code line which is !held (logic not held).
2.) Restricting.
*/

public abstract class NBHAbstractSignalCommon {
    static NBHAbstractSignalCommon getExistingSignal(String module, String userIdentifier, String parameter, String signal) {
        NBHSignalMast signalMast = new NBHSignalMast(signal);
        if (signalMast.valid()) return signalMast;
        NBHSignalHead signalHead = new NBHSignalHead(signal);
        if (signalHead.valid()) return signalHead;
        (new CTCException(module, userIdentifier, parameter, Bundle.getMessage("NBHAbstractSignalCommonException1") + " " + signal + " " + Bundle.getMessage("NBHAbstractSignalCommonException2"))).logError(); // NOI18N
        return signalHead;      // Neither, just return this, it works in "hobbled" mode.  Nothing will crash using it!
    }
    
    abstract public void addPropertyChangeListener(PropertyChangeListener l);
    abstract public void removePropertyChangeListener(PropertyChangeListener l);
    abstract public boolean getHeld();
    abstract public void setHeld(boolean held);
    abstract public String getDisplayName();
    abstract public int[] getValidStates();
    abstract public String[] getValidStateKeys();
    abstract public String[] getValidStateNames();
    abstract public void setAppearance(int newAppearance);
    abstract public Object getBean();
    abstract public boolean isDanger();         // Signal head/mast displaying all SOLID red (all stop).
}
