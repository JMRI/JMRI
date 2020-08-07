package jmri.jmrit.ctc;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;


/**
 * Most times the user does not have the System Console displayed when the CTC
 * system is being started up.  As such, errors logged to the CTCException class
 * just "disappear" into the ether on that console, and the user has no
 * knowledge of any problems.
 * 
 * In this object, I will also gather up all of the errors, warnings and info
 * messages that my system generates in CTCException, and display them to the
 * user via a dialog box of some form, after the CTC system is fully started.
 * 
 * For safety, I implement InstanceManagerAutoDefault so that the objects
 * default constructor is called (for future safety).  I'm not sure
 * if "class" variables below are initialized properly if this is not done.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 */
public class CTCExceptionBuffer implements InstanceManagerAutoDefault {
    ArrayList<String> _mArrayListOfStrings = new ArrayList<>();
    public CTCExceptionBuffer() {}
    public boolean isEmpty() { return _mArrayListOfStrings.isEmpty(); }
    public void clear() { _mArrayListOfStrings.clear(); }
    public void logString(String string) { _mArrayListOfStrings.add(string); }
}
