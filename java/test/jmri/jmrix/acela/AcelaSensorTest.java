package jmri.jmrix.acela;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AcelaSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}


    @Test
    public void test2StringCTor() {
        AcelaSensor t2 = new AcelaSensor("AS1","test");
        Assert.assertNotNull("exists",t2);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        t = new AcelaSensor("AS1");
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AcelaSensorTest.class);

}
