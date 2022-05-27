package jmri.jmrit.ctc;

import jmri.InstanceManager;

/**
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019, 2020
 * 
 * The purpose of this class is to provide a single point of interface to the
 * JMRI error logging system.
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
    justification="Logging Strings provided by calling classes.")
public class CTCException extends Exception {

    private final String _mModule;
    private final String _mUserIdentifier;
    private final String _mParameter;
    private final String _mReason;
    private static final CTCExceptionBuffer _ctcExceptionBuffer = InstanceManager.getDefault(CTCExceptionBuffer.class);

    public CTCException(String module, String userIdentifier, String parameter, String reason) {
        _mModule = module;
        _mUserIdentifier = userIdentifier;
        _mParameter = parameter;
        _mReason = reason;
    }

    public String getExceptionString() {
        return _mModule + ", " + _mUserIdentifier + _mParameter + ", " + _mReason;
    }

    public void logError() {
        logError(getExceptionString());
    }

    public void logWarning() {
        logWarning(getExceptionString());
    }

    static public void logError(String string) {
        log.error(string);
        _ctcExceptionBuffer.logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.ERROR, string);
    }

    static public void logWarning(String string) {
        log.warn(string);
        _ctcExceptionBuffer.logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.WARN, string);
    }

    static public void logInfo(String string) {
        log.info(string);
        _ctcExceptionBuffer.logString(CTCExceptionBuffer.ExceptionBufferRecordSeverity.INFO, string);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CTCException.class);

}
