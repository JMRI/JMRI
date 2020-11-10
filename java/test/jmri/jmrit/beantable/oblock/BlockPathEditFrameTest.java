package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
public class BlockPathEditFrameTest {

    @Test
    public void testCTor() {
        OBlock ob = new OBlock("OB1");
        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob,
                null,
                new TableFrames.PathTurnoutJPanel("OB1"),
                null,
                null);
        Assert.assertNotNull("exists", bpef);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockPathEditFrameTest.class);

}
