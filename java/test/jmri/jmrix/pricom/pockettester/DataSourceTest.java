package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * JUnit tests for the DataSource class
 *
 * @author Bob Jacobsen Copyright 2005
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class DataSourceTest {

    @Test
    public void testCreate() {
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
        DataSource f = new DataSource();
        String message;

        message = "nothing interesing";
        f.nextLine(message);
        Assert.assertTrue("pass misc ", !message.equals(f.version.getText()));

        message = TestConstants.version;
        f.nextLine(message);
        Assert.assertTrue("show version ", message.equals(f.version.getText()));

        f.dispose();
    }

    // avoid spurious error messages
    @BeforeEach
    public void setup() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        DataSource.setInstance(null);
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
