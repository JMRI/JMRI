package jmri.jmrit.symbolicprog;

/**
 * Qualify a variable on greater than or equal a number
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
 *
 */
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
