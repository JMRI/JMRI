package jmri;

import java.util.*;

/**
 * Base for JMRI-specific exceptions. No functionality, just used to confirm
 * type-safety.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
public class JmriException extends Exception {

    private final List<String> errors;

    public JmriException(String s, Throwable t) {
        super(s, t);
        errors = null;
    }

    public JmriException(String s) {
        super(s);
        errors = null;
    }

    public JmriException(Throwable t) {
        super(t);
        errors = null;
    }

    public JmriException() {
        errors = null;
    }

    public JmriException(String s, List<String> errors) {
        super(s);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public JmriException(String s, List<String> errors, Throwable t) {
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
