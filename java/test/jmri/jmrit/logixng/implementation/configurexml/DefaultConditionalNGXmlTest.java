package jmri.jmrit.logixng.implementation.configurexml;

import jmri.JmriException;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Test ActionTurnoutXml
 * 
 * @author Daniel Bergqvist 2019
 */
public class DefaultConditionalNGXmlTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testStore() {
        thrown.expect(UnsupportedOperationException.class);
        DefaultConditionalNGXml b = new DefaultConditionalNGXml();
        Assert.assertNotNull("exists", b);
        b.store((Object) null);
    }
    
    @Test
    public void testLoad() throws JmriConfigureXmlException {
        DefaultConditionalNGXml b;
        
        // Test method load(Element, Object)
        b = new DefaultConditionalNGXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        // Test method load(Element, Element)
        b = new DefaultConditionalNGXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Element) null);
        JUnitAppender.assertMessage("Invalid method called");
        
        // Test loading a conditionalng without system name
        boolean exceptionThrown = false;
        Element e = new Element("conditionalng");
        try {
            b.loadConditionalNG(null, e);
        } catch (JmriException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue("exception thrown", exceptionThrown);
        JUnitAppender.assertWarnMessage("unexpected null in systemName [Element: <conditionalng/>]");
    }
    
    @Test
    public void testLoadOrder() {
        thrown.expect(UnsupportedOperationException.class);
        DefaultConditionalNGXml b = new DefaultConditionalNGXml();
        Assert.assertNotNull("exists", b);
        b.loadOrder();
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
