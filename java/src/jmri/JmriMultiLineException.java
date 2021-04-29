package jmri;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

/**
 * JMRI exception that can have many error messages.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class JmriMultiLineException extends JmriException {

    private final List<String> errors;

    public JmriMultiLineException(String s, List<String> errors) {
        super(s);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public JmriMultiLineException(String s, List<String> errors, Throwable t) {
        super(s, t);
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    public List<String> getErrors() {
        return errors;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return super.getMessage() + ": " + String.join(", ", errors);
    }

    /** {@inheritDoc} */
    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + ": " + String.join(", ", errors);
    }

}
