package jmri.jmrit.logixng.expressions.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DigitalExpressions
 * 
 * @author Daniel Bergqvist 2019
 */
public class DigitalExpressionsTest {

    @Test
    public void testLoad() throws JmriConfigureXmlException {
        AbstractNamedBeanManagerConfigXML b;
        
        b = new AndXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new AntecedentXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new ExpressionLightXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new ExpressionSensorXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new ExpressionTurnoutXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new FalseXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new HoldXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new OrXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new TriggerOnceXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        b = new TrueXml();
        Assert.assertNotNull("exists", b);
        b.load((Element) null, (Object) null);
        JUnitAppender.assertErrorMessage("Invalid method called");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
