/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

public class CTCException extends Exception {
    private final String _mModule;
    private final String _mUserIdentifier;
    private final String _mParameter;
    private final String _mReason;
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
