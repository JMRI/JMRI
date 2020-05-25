package apps.startup;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the StartupActionFactoryScaffold class
 * 
 * @author Daniel Bergqvist Copyright (C) 2020
 */
public class StartupActionFactoryScaffoldTest {

    @Test
    public void testAction() {
        boolean found = false;
        StartupActionModelUtil t = StartupActionModelUtil.getDefault();
        Assert.assertNotNull("exists",t);
        for (Class c : t.getClasses()) {
            if ("java.lang.String".equals(c.getName())) found = true;
        }
        Assert.assertTrue("class is loaded", found);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
