package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WarrantManagerTest {

    @Test
    public void testCTor() {
        WarrantManager t = new WarrantManager();
        assertThat(t).withFailMessage("exists").isNotNull();
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(WarrantManagerTest.class);

}
