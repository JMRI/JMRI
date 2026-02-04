package jmri.jmrix.pricom.pockettester;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the DataSource class
 *
 * @author Bob Jacobsen Copyright 2005
 */
@DisabledIfHeadless
public class DataSourceTest {

    @Test
    public void testCreate() {
        assertNull( DataSource.instance() , "no instance before ctor");
        DataSource d = new DataSource();
        assertNull( DataSource.instance() , "no instance after ctor");
        d.initComponents();
        assertNotNull( DataSource.instance(), "valid instance after init");
        d.dispose();
    }

    // test version handling
    @Test
    public void testVersion() {
        DataSource f = new DataSource();
        String message;

        message = "nothing interesing";
        f.nextLine(message);
        assertNotEquals( message, f.version.getText(), "pass misc ");

        message = TestConstants.version;
        f.nextLine(message);
        assertEquals( message, f.version.getText(), "show version ");

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
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
