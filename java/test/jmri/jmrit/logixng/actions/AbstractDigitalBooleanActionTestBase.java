package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Locale;

import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalBooleanActionBean;

import org.junit.jupiter.api.Test;

/**
 * Base class for classes that tests DigitalBooleanAction
 */
public abstract class AbstractDigitalBooleanActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            fail("Bean is not null " +  bean);
        }, "Exception is thrown");
        assertEquals( "system name is not valid: IQ111", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testBundle() {
        assertEquals( "Logix Action", Bundle.getMessage("DigitalBooleanLogixAction_Short"), "strings are equal");
//        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage("DigitalBooleanLogixAction_Long_Change", "IM1"));
        assertEquals( "Logix Action", Bundle.getMessage(Locale.CANADA, "DigitalBooleanLogixAction_Short"), "strings are equal");
//        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage(Locale.CANADA, "DigitalBooleanLogixAction_Long_Change", "IM1"));

        // The bundle in jmri.jmrit.logixng.actions doesn't
        // currently has a property that uses arguments so test a property
        // in jmri.jmrit.logixng bundle instead.
        assertEquals( "Test Bundle BB AA CC", Bundle.getMessage("TestBundle", "AA", "BB", "CC"), "strings are equal");
        assertEquals( "Test Bundle BB AA CC", Bundle.getMessage(Locale.CANADA, "TestBundle", "AA", "BB", "CC"), "strings are equal");
    }

    @Test
    public void testGetBeanType() {
        assertEquals( "Digital boolean action", ((DigitalBooleanActionBean)_base).getBeanType(), "String matches");
    }

}
