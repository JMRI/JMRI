package jmri.jmrit.logixng.tools;

import jmri.*;

/**
 * Test import of Logix to LogixNG.
 * <P>
 * This class creates a Logix, test that it works, imports it to LogixNG,
 * deletes the original Logix and then test that the new LogixNG works.
 * <P>
 This test tests expression memory
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class ImportExpressionMemoryTest extends ImportExpressionComplexTestBase {

    Memory memory;
    ConditionalVariable cv;
    
    private enum MemoryEnum {
        ConstantEquals(Conditional.Type.MEMORY_EQUALS, ConditionalVariable.EQUAL),
        ConstantCompareLessThan(Conditional.Type.MEMORY_EQUALS, ConditionalVariable.LESS_THAN),
        ConstantCompareLessEqual(Conditional.Type.MEMORY_EQUALS, ConditionalVariable.LESS_THAN_OR_EQUAL),
        ConstantCompareGreaterThan(Conditional.Type.MEMORY_EQUALS, ConditionalVariable.GREATER_THAN),
        ConstantCompareGreaterEqual(Conditional.Type.MEMORY_EQUALS, ConditionalVariable.GREATER_THAN_OR_EQUAL),
        ConstantEqualsInsensitive(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, ConditionalVariable.EQUAL),
        ConstantCompareLessThanInsensitive(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, ConditionalVariable.LESS_THAN),
        ConstantCompareLessEqualInsensitive(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, ConditionalVariable.LESS_THAN_OR_EQUAL),
        ConstantCompareGreaterThanInsensitive(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, ConditionalVariable.GREATER_THAN),
        ConstantCompareGreaterEqualInsensitive(Conditional.Type.MEMORY_EQUALS_INSENSITIVE, ConditionalVariable.GREATER_THAN_OR_EQUAL),
        MemoryEquals(Conditional.Type.MEMORY_COMPARE, ConditionalVariable.EQUAL),
        MemoryCompareLessThan(Conditional.Type.MEMORY_COMPARE, ConditionalVariable.LESS_THAN),
        MemoryCompareLessEqual(Conditional.Type.MEMORY_COMPARE, ConditionalVariable.LESS_THAN_OR_EQUAL),
        MemoryCompareGreaterThan(Conditional.Type.MEMORY_COMPARE, ConditionalVariable.GREATER_THAN),
        MemoryCompareGreaterEqual(Conditional.Type.MEMORY_COMPARE, ConditionalVariable.GREATER_THAN_OR_EQUAL),
        MemoryEqualsInsensitive(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, ConditionalVariable.EQUAL),
        MemoryCompareLessThanInsensitive(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, ConditionalVariable.LESS_THAN),
        MemoryCompareLessEqualInsensitive(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, ConditionalVariable.LESS_THAN_OR_EQUAL),
        MemoryCompareGreaterThanInsensitive(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, ConditionalVariable.GREATER_THAN),
        MemoryCompareGreaterEqualInsensitive(Conditional.Type.MEMORY_COMPARE_INSENSITIVE, ConditionalVariable.GREATER_THAN_OR_EQUAL);
        
        private Conditional.Type type;
        private int num1;
        
        private MemoryEnum(Conditional.Type type, int num1) {
            this.type = type;
            this.num1 = num1;
        }
    }
    
    @Override
    public Enum[] getEnums() {
        return MemoryEnum.values();
    }
    
    @Override
    public void setNamedBeanState(Enum e, Setup setup) throws JmriException {
        MemoryEnum me = MemoryEnum.valueOf(e.name());
        
        if ((me.type == Conditional.Type.MEMORY_EQUALS) || (me.type == Conditional.Type.MEMORY_EQUALS_INSENSITIVE)) {
            cv.setDataString("Some string to TEST");    // The constant string to compare with
        } else {
            InstanceManager.getDefault(MemoryManager.class).provide("IMOtherMemory").setValue("Some string to TEST");
            cv.setDataString("IMOtherMemory");    // The memory to compare with
        }
        
        cv.setNum1(me.num1);
        cv.setNum2(0);
        
        cv.setType(me.type);
        
        switch (me) {
            case ConstantEquals:
            case MemoryEquals:
                switch (setup) {
                    case Init: memory.setValue("Initial string"); break;
                    case Fail1: memory.setValue("Something different"); break;
                    case Fail2: memory.setValue("Something other"); break;
                    case Fail3: memory.setValue("Some STRING to test"); break;      // Fails due to case sensitive
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: memory.setValue("Some string to TEST"); break;
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareLessThan:
            case MemoryCompareLessThan:
                switch (setup) {
                    case Init: memory.setValue("Zzzz string"); break;
                    case Fail1: memory.setValue("Something later"); break;
                    case Fail2: memory.setValue("Some string to test"); break;
                    case Fail3: memory.setValue("Some string to TEST"); break;   // Equals
                    case Succeed1:
                    case Succeed2:
                    case Succeed3: memory.setValue("Some other string to test"); break; // Less than since "other" comes before "string"
                    case Succeed4: memory.setValue("Some stRing to test"); break;       // Less than since case sensitive
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareLessEqual:
            case MemoryCompareLessEqual:
                switch (setup) {
                    case Init: memory.setValue("Zzzz string"); break;
                    case Fail1: memory.setValue("Something later"); break;
                    case Fail2: memory.setValue("Some string to test"); break;      // "test" is greater than "TEST" (case sensitive)
                    case Fail3: memory.setValue("Something else"); break;
                    case Succeed1:
                    case Succeed2: memory.setValue("Some other string to test"); break; // Less than since "other" comes before "string"
                    case Succeed3: memory.setValue("Some stRing to test"); break;       // Less than since case sensitive
                    case Succeed4: memory.setValue("Some string to TEST"); break;       // Equal
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareGreaterThan:
            case MemoryCompareGreaterThan:
                switch (setup) {
                    case Init: memory.setValue("Aaaa string"); break;
                    case Fail1: memory.setValue("Some earlier string"); break;
                    case Fail2: memory.setValue("Some string TO test"); break;  // Before since case sensitive
                    case Fail3: memory.setValue("Some string to TEST"); break;  // Equals
                    case Succeed1:
                    case Succeed2:
                    case Succeed3: memory.setValue("Some zzz string to test"); break;   // Greater than since "zzz" comes after "string"
                    case Succeed4: memory.setValue("Some string to test"); break;       // Greater than since case sensitive
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareGreaterEqual:
            case MemoryCompareGreaterEqual:
                switch (setup) {
                    case Init: memory.setValue("Aaaa string"); break;
                    case Fail1:
                    case Fail2: memory.setValue("Some other string to test"); break;
                    case Fail3: memory.setValue("Some string aaa"); break;  // Less than
                    case Succeed1:
                    case Succeed2: memory.setValue("Something later"); break;
                    case Succeed3: memory.setValue("Some string to TEST"); break;   // Equals
                    case Succeed4: memory.setValue("Some string to test"); break;   // Greater than since case sensitive
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantEqualsInsensitive:
            case MemoryEqualsInsensitive:
                switch (setup) {
                    case Init: memory.setValue("Zzzz string"); break;
                    case Fail1: memory.setValue("Something later"); break;  // Greater than
                    case Fail2: memory.setValue("Some string aaa"); break;  // Less than
                    case Fail3: memory.setValue("Something else"); break;
                    case Succeed1:
                    case Succeed2: memory.setValue("Some string to TEST"); break;   // Equal
                    case Succeed3: memory.setValue("Some string to test"); break;   // Equal since case sensitive
                    case Succeed4: memory.setValue("Some stRing to TEST"); break;   // Equal since case sensitive
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareLessThanInsensitive:
            case MemoryCompareLessThanInsensitive:
                switch (setup) {
                    case Init: memory.setValue("Zzzz string"); break;
                    case Fail1: memory.setValue("Something later111"); break;
                    case Fail2: memory.setValue("Some string to test"); break;
                    case Fail3: memory.setValue("Some string to TEST"); break;   // Equals
                    case Succeed1:
                    case Succeed2:
                    case Succeed3: memory.setValue("Some other string to test"); break; // Less than since "other" comes before "string"
                    case Succeed4: memory.setValue("Some aaa string"); break;       // Equal
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareLessEqualInsensitive:
            case MemoryCompareLessEqualInsensitive:
                switch (setup) {
                    case Init: memory.setValue("Zzzz string"); break;
                    case Fail1:
                    case Fail2: memory.setValue("Something later"); break;
                    case Fail3: memory.setValue("Something else"); break;
                    case Succeed1: memory.setValue("Some string to test"); break;       // Equal since case sensitive
                    case Succeed2: memory.setValue("Some other string to test"); break; // Less than since "other" comes before "string"
                    case Succeed3: memory.setValue("Some stRing to test"); break;       // Less than since case sensitive
                    case Succeed4: memory.setValue("Some string to TEST"); break;       // Equal
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareGreaterThanInsensitive:
            case MemoryCompareGreaterThanInsensitive:
                switch (setup) {
                    case Init: memory.setValue("Aaaa string"); break;
                    case Fail1: memory.setValue("Some earlier string"); break;
                    case Fail2: memory.setValue("Some string to test"); break;  // Equals since case sensitive
                    case Fail3: memory.setValue("Some string to TEST"); break;  // Equals
                    case Succeed1:
                    case Succeed2:
                    case Succeed3:
                    case Succeed4: memory.setValue("Some zzz string to test"); break;   // Greater than since "zzz" comes after "string"
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            case ConstantCompareGreaterEqualInsensitive:
            case MemoryCompareGreaterEqualInsensitive:
                switch (setup) {
                    case Init: memory.setValue("Aaaa string"); break;
                    case Fail1:
                    case Fail2: memory.setValue("Some other string to test"); break;
                    case Fail3: memory.setValue("Some string aaa"); break;  // Less than
                    case Succeed1:
                    case Succeed2: memory.setValue("Something later"); break;
                    case Succeed3: memory.setValue("Some string to TEST"); break;   // Equals
                    case Succeed4: memory.setValue("Some string to test"); break;   // Equals than since case sensitive
                    default: throw new RuntimeException("Unknown enum: "+e.name());
                }
                break;
                
            default:
                throw new RuntimeException("Unknown enum: "+e.name());
        }
    }
    
    @Override
    public ConditionalVariable newConditionalVariable() {
        memory = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        InstanceManager.getDefault(MemoryManager.class).provide("IMOtherMemory");
        cv = new ConditionalVariable();
        cv.setName("IM1");
        return cv;
    }
    
}
