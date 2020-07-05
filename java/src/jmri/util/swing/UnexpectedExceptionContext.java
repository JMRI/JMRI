package jmri.util.swing;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Extends ExceptionContext class for exceptions that are not expected, and
 * therefore have no suggestions for the user.
 *
 * @author Gregory Madsen Copyright (C) 2012
 *
 */
@API(status = EXPERIMENTAL)
public class UnexpectedExceptionContext extends ExceptionContext {

    @Override
    public String getTitle() {
        return Bundle.getMessage("UnexpectedExceptionOperationTitle",super.getTitle());
    }

    public UnexpectedExceptionContext(Exception ex, String operation) {
        super(ex, operation, Bundle.getMessage("UnexpectedExceptionOperationHint"));
        this._preface = Bundle.getMessage("UnexpectedExceptionOperationPreface");
    }
}
