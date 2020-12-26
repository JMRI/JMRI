package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LncvMessageContentsTest {

    @Test
    public void testCTorIllegalArgument() {
        LocoNetMessage lm = new LocoNetMessage(3); // LncvMessage length should be 15
        Assert.assertThrows(IllegalArgumentException.class, () -> new LncvMessageContents(lm));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LncvMessageContentsTest.class);

}
