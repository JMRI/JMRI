package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the RouteWhereUsed Class
 *
 * @author Dave Sand Copyright (C) 2020
 */
public class RouteWhereUsedTest {

    @Test
    public void testRouteWhereUsed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        RouteWhereUsed ctor = new RouteWhereUsed();
        Assert.assertNotNull("exists", ctor);
        Route route = InstanceManager.getDefault(jmri.RouteManager.class).getRoute("Sensors");
        JTextArea result = RouteWhereUsed.getWhereUsed(route);
        Assert.assertFalse(result.getText().isEmpty());
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        java.io.File f = new java.io.File("java/test/jmri/jmrit/whereused/load/WhereUsedTesting.xml");
        cm.load(f);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RouteWhereUsedTest.class);
}
