package jmri;

import java.util.*;

/**
 * Base for JMRI-specific unchecked exceptions. No functionality, just used to
 * confirm type-safety.
 *
 * @author Bob Jacobsen      Copyright (C) 2001, 2008, 2010
 * @author Daniel Bergqvist  Copyright (C) 2026
 */
public class JmriUncheckedException extends RuntimeException {

    private final List<String> errors;

    public JmriUncheckedException(String s, Throwable t) {
        super(s, t);
        errors = null;
    }

    public JmriUncheckedException(String s) {
        super(s);
        errors = null;
    }

    public JmriUncheckedException(Throwable t) {
        super(t);
        errors = null;
    }

    public JmriUncheckedException() {
        errors = null;
    }

    public JmriUncheckedException(String s, List<String> errors) {
        super(s);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public JmriUncheckedException(String s, List<String> errors, Throwable t) {
        super(s, t);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        if (errors != null) {
            return super.getMessage() + ": " + String.join(", ", errors);
        } else {
            return super.getMessage();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalizedMessage() {
        if (errors != null) {
            return super.getLocalizedMessage() + ": " + String.join(", ", errors);
        } else {
            return super.getLocalizedMessage();
        }
    }

}
