package jmri.jmrit.operations;

/**
 * Extends ExceptionContext class for exceptions that are not expected, and
 * therefore have no suggestions for the user.
 * 
 * @author Gregory Madsen Copyright (C) 2012
 * 
 */
public class UnexpectedExceptionContext extends ExceptionContext {

	@Override
	public String getTitle() {
		return super.getTitle() + " (Unexpected)";
	}

	public UnexpectedExceptionContext(Exception ex, String operation) {
		super(
				ex,
				operation,
				"This exception was unexpected and is probably a bug in the code.");

		this._preface = "An unexpected error occurred during the following operation.";

	}
}
