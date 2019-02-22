/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

public class CTCException extends Exception {
    private final String _mModule;
    private final String _mUserIdentifier;
    private final String _mParameter;
    private final String _mReason;
//  I'm practicing safe programming practices here, and Travis and Java compiler complain about each other here:    
//  Travis complains that the default constructor is not called, and I can't do "this();" as the first
//  line in the CTCException(String....) constructor to call it to get aroud Travis, because I properly
//  used "final" in the variable declarations above. So there seems to be NO way to prevent a user from calling
//  the default constructor IMPROPERLY.
//  DON'T USE THE DEFAULT CONSTRUCTOR PLEASE!
//  private CTCException() { _mModule = ""; _mUserIdentifier = ""; _mParameter = ""; _mReason = ""; }  // Shouldn't use it this way!
    public CTCException(String module, String userIdentifier, String parameter, String reason) {
        _mModule = module;
        _mUserIdentifier = userIdentifier;
        _mParameter = parameter;
        _mReason = reason;
    }
    public String getExceptionString() { return _mModule + ", " + _mUserIdentifier + _mParameter + ", " + _mReason; }
    public void logError() { org.slf4j.LoggerFactory.getLogger(CTCException.class).error(getExceptionString()); }
    public void logWarning() { org.slf4j.LoggerFactory.getLogger(CTCException.class).warn(getExceptionString()); }
    static public void logError(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).error(string); }
    static public void logWarning(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).warn(string); }
    static public void logInfo(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).info(string); }
}
