package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * JUnit tests for the DataSource class
 *
 * @author	Bob Jacobsen Copyright 2005
 */
public class DataSourceTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("no instance before ctor", DataSource.instance() == null);
        DataSource d = new DataSource();
        Assert.assertTrue("no instance after ctor", DataSource.instance() == null);
        d.initComponents();
        Assert.assertTrue("valid instance after init", DataSource.instance() != null);
        d.dispose();
    }

    // test version handling
    @Test
    public void testVersion() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DataSource f = new DataSource();
        String message;

        message = "nothing interesing";
        f.nextLine(message);
        Assert.assertTrue("pass misc ", !message.equals(f.version.getText()));

        message = PackageTest.version;
        f.nextLine(message);
        Assert.assertTrue("show version ", message.equals(f.version.getText()));

        f.dispose();
    }

    // avoid spurious error messages
    @Before
    public void setup() {
        JUnitUtil.setUp();
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        DataSource.existingInstance = null;
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
