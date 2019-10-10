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
 * 0). Comparisons with the value of a non-existent variable always fail.
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2014
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
        this.value = Integer.toUnsignedLong(value);
    }

    @Override
    public boolean currentDesiredState() {
        if (returnFromExistsLogic()) {
            return valueOfExistsLogic();
        }

        return availableStateFromValue(watchedVal.getLongValue());
    }

    @Override
    protected boolean availableStateFromValue(Object now) {
        if (returnFromExistsLogic()) {
            return valueOfExistsLogic();
        }

        long nowVal = 0;
        if (now instanceof Integer) {
            nowVal = Integer.toUnsignedLong((int) now);
        } else if (now instanceof Long) {
            nowVal = (Long) now;
        }

        int compare = Long.compareUnsigned(nowVal, value);

        switch (test) {
            case GE:
                return compare >= 0;
            case LE:
                return compare <= 0;
            case GT:
                return compare > 0;
            case LT:
                return compare < 0;
            case EQ:
                return compare == 0;
            case NE:
                return compare != 0;
            default:
                log.error("Unexpected switch value: {}", test);
                return false;
        }

    }

    @Override
    public void update() {
        setWatchedAvailable(currentDesiredState());
    }

    long value;

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

    private final static Logger log = LoggerFactory.getLogger(VariableTableModel.class);
}
