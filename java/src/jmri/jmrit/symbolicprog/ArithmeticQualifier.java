// ArithmeticQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Mechanism to qualify on a variable on greater than or equal a number
 *
 * @author			Bob Jacobsen   Copyright (C) 2010, 2014
 * @version			$Revision$
 *
 */
public abstract class ArithmeticQualifier extends AbstractQualifier {

    public enum Test {
        GE("ge"), // greater than or equal
        LE("le"),
        GT("gt"), 
        LT("lt"),
        EQ("eq"),
        NE("ne");
        
        Test(String relation) {
            this.relation = relation;
        }
        String relation;
        
        static Test decode(String r) {
            for (Test t : Test.values()) {
                if (t.relation.equals(r)) return t;
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
        return availableStateFromObject(watchedVal.getValueObject());
    }

    protected boolean availableStateFromObject(Object o) {
        int now = ((Integer) o ).intValue();
        return availableStateFromValue(now);
    }
    
    protected boolean availableStateFromValue(int now) {
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
        }
        return false;       // shouldn't happen?
    }
    
    public void update() {
        setWatchedAvailable(availableStateFromValue(watchedVal.getIntValue()));
    }
    
    int value;
}
