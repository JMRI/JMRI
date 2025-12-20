package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.Test;

import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalActionBean;

/**
 * Base class for classes that tests DigitalAction
 */
public abstract class AbstractDigitalActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);

    @Test
    public void testBadSystemName() {
        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () -> {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            assertNotNull( bean, "Bean is not null");
            });
        assertEquals( "system name is not valid: IQ111", e.getMessage(), "Exception is correct");
    }

    @Test
    public void testBundle() {
        assertEquals( "Memory", Bundle.getMessage("ActionMemory_Short"), "strings are equal");
        assertEquals( "Set memory IM1 to null", Bundle.getMessage("ActionMemory_Long_Null", "IM1"), "strings are equal 2");
        assertEquals( "Memory", Bundle.getMessage(Locale.CANADA, "ActionMemory_Short"), "strings are equal 3");
        assertEquals( "Set memory IM1 to null", Bundle.getMessage(Locale.CANADA, "ActionMemory_Long_Null", "IM1"), "strings are equal 4");
    }

    @Test
    public void testGetBeanType() {
        assertTrue( "Digital action".equals(((DigitalActionBean)_base).getBeanType()), "String matches");
    }

}
