package apps.startup;

import jmri.util.startup.StartupActionModelUtil;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

        // suppress deprecation warning that's expected
        jmri.util.JUnitAppender.assertWarnMessageStartsWith("apps.startup.StartupActionFactoryScaffold is deprecated, please remove references to it");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
