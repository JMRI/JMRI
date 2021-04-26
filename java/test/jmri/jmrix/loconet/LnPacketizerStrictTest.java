package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Paul Bender Copyright (C) 2018
 */
public class LnPacketizerStrictTest extends LnPacketizerTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        lnp = new LnPacketizerStrict(memo);
    }

    @AfterEach
    @Override
    public void tearDown() {
        lnp = null;
        JUnitUtil.tearDown();
    }

}
