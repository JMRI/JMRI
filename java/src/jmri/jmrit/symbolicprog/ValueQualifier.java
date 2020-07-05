package jmri.jmrit.symbolicprog;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Qualify a variable on greater than or equal a number
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 *
 */
@API(status = MAINTAINED)
public class ValueQualifier extends ArithmeticQualifier {

    public ValueQualifier(VariableValue qualifiedVal, VariableValue watchedVal, int value, String relation) {
        super(watchedVal, value, relation);

        this.qualifiedVal = qualifiedVal;
        setWatchedAvailable(currentDesiredState());
    }

    VariableValue qualifiedVal;

    @Override
    public void setWatchedAvailable(boolean enable) {
        qualifiedVal.setAvailable(enable);
    }

    @Override
    protected boolean currentAvailableState() {
        return qualifiedVal.getAvailable();
    }

}
