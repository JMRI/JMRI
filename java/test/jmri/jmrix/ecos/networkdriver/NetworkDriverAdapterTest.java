package jmri.jmrix.ecos.networkdriver;

import jmri.jmrix.ecos.EcosSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class NetworkDriverAdapterTest {

    private EcosSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        NetworkDriverAdapter t = new NetworkDriverAdapter();
        Assertions.assertNotNull(t, "exists");
    }

    @Test
    public void testMemoCTor() {
        NetworkDriverAdapter tm = new NetworkDriverAdapter();
        Assertions.assertNotNull(tm, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new EcosSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NetworkDriverAdapterTest.class);

}
