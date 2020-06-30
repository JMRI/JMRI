package jmri.jmrit.logixng;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class PluginManagerTest {
    
    @Test
    public void testPluginManager()
                throws FileNotFoundException, IOException, ClassNotFoundException,
                InstantiationException, IllegalAccessException {
        
        // The JAR file used in this test is from the project:
        // https://github.com/danielb987/JMRI_LogixNG_Plugins
        
        PluginManager pluginManager = new PluginManager();
        
        PluginManager.JarFile jarFile = pluginManager.addJarFile("java/test/jmri/jmrit/logixng/JMRI_LogixNG_Plugins.jar");
        
        Assert.assertNotNull("pluginManager exists", pluginManager);
        Assert.assertNotNull("jarFile exists", jarFile);
        
        AtomicBoolean flag = new AtomicBoolean(false);
        try {
            // Try adding a file that doesn't exists
            pluginManager.addJarFile("i_dont_exist.jar");
        } catch (FileNotFoundException e) {
            flag.set(true);
        }
        Assert.assertTrue("exception thrown", flag.get());
    }
    
    @Test
    public void testClassDefinition() {
        PluginManager.ClassDefinition cd = new PluginManager.ClassDefinition(true, PluginManager.ClassType.ACTION, "My string");
        Assert.assertTrue("is enabled", cd.getEnabled());
        Assert.assertEquals("equals", PluginManager.ClassType.ACTION, cd.getType());
        Assert.assertEquals("equals", "My string", cd.getName());
        cd = new PluginManager.ClassDefinition(false, PluginManager.ClassType.EXPRESSION, "My integer");
        Assert.assertFalse("is not enabled", cd.getEnabled());
        Assert.assertEquals("equals", PluginManager.ClassType.EXPRESSION, cd.getType());
        Assert.assertEquals("equals", "My integer", cd.getName());
        cd.setEnabled(true);
        Assert.assertTrue("is enabled", cd.getEnabled());
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
        JUnitUtil.tearDown();
    }
    
}
