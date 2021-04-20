package jmri.jmrit.logixng.actions;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import jmri.NamedBean;
import jmri.jmrit.logixng.AbstractBaseTestBase;
import jmri.jmrit.logixng.DigitalBooleanActionBean;

/**
 * Base class for classes that tests DigitalBooleanAction
 */
public abstract class AbstractDigitalBooleanActionTestBase extends AbstractBaseTestBase {

    public abstract NamedBean createNewBean(String systemName);
    
    @Test
    public void testBadSystemName() {
        boolean hasThrown = false;
        try {
            // Create a bean with bad system name. This must throw an exception
            NamedBean bean = createNewBean("IQ111");
            // We should never get here.
            Assert.assertNotNull("Bean is not null", bean);
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Exception is correct", "system name is not valid: IQ111", e.getMessage());
            hasThrown = true;
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testBundle() {
        Assert.assertEquals("strings are equal", "On change", Bundle.getMessage("DigitalBooleanOnChange_Short"));
//        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage("DigitalBooleanOnChange_Long_Change", "IM1"));
        Assert.assertEquals("strings are equal", "On change", Bundle.getMessage(Locale.CANADA, "DigitalBooleanOnChange_Short"));
//        Assert.assertEquals("strings are equal", "Set memory IM1 to null", Bundle.getMessage(Locale.CANADA, "DigitalBooleanOnChange_Long_Change", "IM1"));
        
        // The bundle in jmri.jmrit.logixng.actions doesn't
        // currently has a property that uses arguments so test a property
        // in jmri.jmrit.logixng bundle instead.
        Assert.assertEquals("strings are equal", "Test Bundle BB AA CC", Bundle.getMessage("TestBundle", "AA", "BB", "CC"));
        Assert.assertEquals("strings are equal", "Test Bundle BB AA CC", Bundle.getMessage(Locale.CANADA, "TestBundle", "AA", "BB", "CC"));
    }
    
    @Test
    public void testGetBeanType() {
        Assert.assertTrue("String matches", "Digital boolean action".equals(((DigitalBooleanActionBean)_base).getBeanType()));
    }
    
}
