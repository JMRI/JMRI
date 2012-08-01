package jmri.jmrit.operations;

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

	protected Exception _exception;

	// The Exception being wrapped
	public Exception getException() {
		return _exception;
	}

	// Used to give a more friendly message
	// This may be replaced in derived classes
	protected String _preface = "An error occurred during the following operation.";

	public String getPreface() {
		return _preface;
	}

	// What was happening when the exception occurred
	protected String _operation;

	public String getOperation() {
		return _operation;
	}

	// A suggestion to the user to correct the problem.
	protected String _hint = "No hint";

	public String getHint() {
		return _hint;
	}

	public String getTitle() {
		// May be overridden in derived classes
		return _exception.getClass().getSimpleName();
	}

	// The system appropriate newline separator, used to format messages for
	// display.
	protected String _nl = System.getProperty("line.separator");

	/**
	 * Returns a user friendly summary of the Exception. Empty parts are not
	 * included. (Maybe later?)
	 */
	public String getSummary() {
		return _preface + _nl + _operation + _nl + _exception.getMessage()
				+ _nl + _hint;
	}

	public ExceptionContext(Exception ex, String operation, String hint) {
		this._exception = ex;
		this._operation = operation;
		this._hint = hint;
	}

	/**
	 * Returns up to the given number of stack trace elements concatenated into
	 * one string.
	 * 
	 */
	public String getStackTraceAsString(int maxLevels) {
		String eol = System.getProperty("line.separator");
		String result = ""; // There must be a better way using some sort of a
							// string builder class, later.....

		StackTraceElement[] stElements = _exception.getStackTrace();

		int limit = Math.min(maxLevels, stElements.length);
		for (int i = 0; i < limit; i++) {
			result = result + " at " + stElements[i].toString() + eol;
		}

		// If there are more levels than included, add a note to the end
		if (stElements.length > limit) {
			result = result + " plus " + (stElements.length - limit) + " more.";
		}
		return result;
	}

	public String getStackTraceString() {
		return getStackTraceAsString(Integer.MAX_VALUE);
	}
}
