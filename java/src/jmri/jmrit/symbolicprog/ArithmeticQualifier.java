package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mechanism to qualify on the value of a number.
 * <p>
 * The usual arithmetic operations are possible: ge, le, gt, lt, eq, ne. The
 * sense of this is comparing "current value" to "constant", for example
 * "current value gt 3".
 * <p>
 * You can also check whether the value "exists" (value of 1) or not (value of
 * 0). Comparisons with the value of a non-existant variable always fail.
 *
 * @author	Bob Jacobsen Copyright (C) 2010, 2014
 *
 */
public abstract class ArithmeticQualifier extends AbstractQualifier {

    public enum Test {

        GE("ge"), // greater than or equal
        LE("le"),
        GT("gt"),
        LT("lt"),
        EQ("eq"),
        NE("ne"),
        EXISTS("exists");

        Test(String relation) {
            this.relation = relation;
        }
        String relation;

        static Test decode(String r) {
            for (Test t : Test.values()) {
                if (t.relation.equals(r)) {
                    return t;
                }
            }
            return null;
        }
    }

    Test test;

    public ArithmeticQualifier(VariableValue watchedVal, int value, String relation) {
        super(watchedVal);

        this.test = Test.decode(relation);
        this.value = value;
    }

    public boolean currentDesiredState() {
        if (returnFromExistsLogic()) {
            return valueOfExistsLogic();
        }

        return availableStateFromValue(watchedVal.getIntValue());
    }

    protected boolean availableStateFromValue(int now) {
        if (returnFromExistsLogic()) {
            return valueOfExistsLogic();
        }

        switch (test) {
            case GE:
                return now >= value;
            case LE:
                return now <= value;
            case GT:
                return now > value;
            case LT:
                return now < value;
            case EQ:
                return now == value;
            case NE:
                return now != value;
            default:
                log.error("Unexpected switch value: {}", test);
                return false;
        }

    }

    public void update() {
        setWatchedAvailable(currentDesiredState());
    }

    int value;

    private boolean returnFromExistsLogic() {
        if (test == Test.EXISTS) {
            return true;
        }
        if (watchedVal == null) {
            return true;
        }
        return false;
    }

    boolean warnedDoesntExist = false;

    private boolean valueOfExistsLogic() {
        if (test == Test.EXISTS) {
            if (value == 0 && watchedVal == null) {
                return true;
            }
            if (value != 0 && watchedVal != null) {
                return true;
            }
            return false;
        }
        // here it's an arithmetic op on a variable  
        if (watchedVal == null) {
            if (!warnedDoesntExist) {
                warnedDoesntExist = true;
                log.error("Arithmetic " + test + " operation when watched value doesn't exist");
            }
            return true;  // this determines default for what happens when qualifier (watched) Variable isn't present
        }
        return false;  // should never be reached, because should only be invoked after returnFromExistsLogic() == true
    }

    private final static Logger log = LoggerFactory.getLogger(VariableTableModel.class.getName());
}
