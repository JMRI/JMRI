package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
public class BlockPathEditFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob = new OBlock("OB1");
        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob,
                null,
                new TableFrames.PathTurnoutJPanel(null),
                null,
                null);
        Assert.assertNotNull("exists", bpef);
    }

    @Test
    public void testPathCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob = new OBlock("OB1");
        OPath path = new OPath(ob, "path");
        TableFrames tf = new TableFrames();
        BlockPathTableModel bptm = new BlockPathTableModel(ob, tf);
        BlockPathEditFrame bpef = new BlockPathEditFrame(
                "Test BPEF",
                ob,
                path,
                new TableFrames.PathTurnoutJPanel(null),
                bptm,
                tf);
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
