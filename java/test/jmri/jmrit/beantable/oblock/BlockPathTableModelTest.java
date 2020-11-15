package jmri.jmrit.beantable.oblock;

import jmri.jmrit.logix.OBlock;
import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.Assert;

import java.awt.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Egbert Broerse Copyright (C) 2020
 */
public class BlockPathTableModelTest {

    @Test
    public void testCTor() {
        BlockPathTableModel bptm = new BlockPathTableModel();
        Assert.assertNotNull("BP model exists", bptm);
    }

    @Test
    public void testTfCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OBlock ob = new OBlock("OB1");
        TableFrames tf = new TableFrames();
        BlockPathTableModel bptm = new BlockPathTableModel(ob, tf);
        Assert.assertNotNull("TF BP model exists", bptm);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockPathTableModelTest.class);

}
