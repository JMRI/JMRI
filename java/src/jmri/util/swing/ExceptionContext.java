package jmri.util.swing;

import javax.annotation.Nonnull;

import jmri.jmrit.mailreport.ReportContext;
import jmri.util.StringUtil;

/**
 * Wraps an Exception and allows extra contextual information to be added, such
 * as what was happening at the time of the Exception, and a hint as to what the
 * user might do to correct the problem. Also implements a number of methods to
 * format the data of an Exception.
 *
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
public class ExceptionContext {

    protected final Throwable exception;
    protected String prefaceString = "An error occurred during the following operation."; // NOI18N
    protected String operation;
    protected String hint = "";

    /**
     * Create a new Exception Context.
     * @param ex the Throwable Exception which has occurred.
     * @param operation An Operation which was taking place at the time of the
     *                  Exception.  Use empty String if unknown.
     * @param hint      A hint as to what might have caused the Exception.
     *                  Use empty String if unknown.
     */
    public ExceptionContext(@Nonnull Throwable ex, @Nonnull String operation, @Nonnull String hint) {
        this.exception = ex;
        this.operation = operation;
        this.hint = hint;
    }

    /**
     * Get the Exception being wrapped.
     * @return the Exception.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Used to give a more friendly message.
     * @return the Preface String.
     */
    public String getPreface() {
        return prefaceString;
    }

    /**
     * Get what was happening when the exception occurred.
     * @return empty String if unknown.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Get suggestion to the user to correct the problem.
     * @return empty string if no hint, else the hint text.
     */
    @Nonnull
    public String getHint() {
        return hint;
    }

    /**
     * Get a String to use as the Title for this Context.
     * @return Localised Exception message, truncated to 80 chars.
     */
    public String getTitle() {
        String msg = exception.getLocalizedMessage();
        return msg.substring(0, Math.min(msg.length(), 80));
    }

    /**
     * Returns a user friendly summary of the Exception.
     * Empty data is excluded.
     * @return A string summary.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (!getPreface().isBlank()) {
            sb.append(getPreface())
                .append(System.lineSeparator()).append(System.lineSeparator());
        }

        if (!getOperation().isBlank()) {
            sb.append(getOperation()).append(System.lineSeparator());
        }

        if (!getHint().isBlank()) {
            sb.append(getHint()).append(System.lineSeparator());
        }

        String msg = getException().getMessage();
        if ( msg != null && !msg.equals(getException().getLocalizedMessage())) {
            sb.append(getException().getLocalizedMessage()).append(System.lineSeparator());
        }

        sb.append(getException().getMessage()).append(System.lineSeparator());
        sb.append(getException().getClass().getName()).append(System.lineSeparator());

        Throwable cause = getException().getCause();
        if (cause != null) {
            sb.append(cause.toString()).append(System.lineSeparator());
        }
        sb.append(getException().toString());
        return StringUtil.stripHtmlTags(sb.toString());
    }

    /**
     * Returns up to the given number of stack trace elements concatenated into
     * one string.
     * @param maxLevels The number of stack trace elements to return.
     * @return A string stack trace.
     *
     */
    public String getStackTraceAsString(int maxLevels) {
        StringBuilder sb = new StringBuilder();

        StackTraceElement[] stElements = exception.getStackTrace();

        int limit = Math.min(maxLevels, stElements.length);
        for (int i = 0; i < limit; i++) {
            sb.append(" at "); // NOI18N
            sb.append(stElements[i].toString());
            sb.append(System.lineSeparator());
        }

        // If there are more levels than included, add a note to the end
        if (stElements.length > limit) {
            sb.append(" plus "); // NOI18N
            sb.append(stElements.length - limit);
            sb.append(" more."); // NOI18N
        }
        return sb.toString();
    }

    /**
     * Get the Full Stack Trace String.
     * @return unabridged Stack Trace String.
     */
    public String getStackTraceString() {
        return getStackTraceAsString(Integer.MAX_VALUE);
    }

    /**
     * Get a String form of this Context for use in pasting to Clipboard.
     * @param includeSysInfo true to include System Information,
     *                       false for just the Exception details.
     * @return String for use in Clipboard text.
     */
    public String getClipboardString(boolean includeSysInfo){
        StringBuilder sb = new StringBuilder();
        sb.append(getSummary());
        sb.append(System.lineSeparator());
        sb.append(getStackTraceString());
        if ( includeSysInfo ) {
            sb.append(System.lineSeparator());
            sb.append(new ReportContext().getReport(true));
        }
        return sb.toString();
    }

}
