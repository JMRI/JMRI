package jmri.jmrit.ctc;

import jmri.InstanceManager;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 * 
 * The purpose of this class is to provide a single point of interface to the
 * JMRI error logging system.
 */
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
    public void logError() { String exceptionString = getExceptionString(); org.slf4j.LoggerFactory.getLogger(CTCException.class).error(exceptionString); InstanceManager.getDefault(CTCExceptionBuffer.class).logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.ERROR, exceptionString); }
    public void logWarning() { String exceptionString = getExceptionString(); org.slf4j.LoggerFactory.getLogger(CTCException.class).warn(exceptionString); InstanceManager.getDefault(CTCExceptionBuffer.class).logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.WARN, exceptionString); }
    static public void logError(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).error(string); InstanceManager.getDefault(CTCExceptionBuffer.class).logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.ERROR, string); }
    static public void logWarning(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).warn(string); InstanceManager.getDefault(CTCExceptionBuffer.class).logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.WARN, string); }
    static public void logInfo(String string) { org.slf4j.LoggerFactory.getLogger(CTCException.class).info(string); InstanceManager.getDefault(CTCExceptionBuffer.class).logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.INFO, string); }
}
