package jmri.jmrit.beantable.oblock;

import jmri.implementation.VirtualSignalMast;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;
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
public class SignalEditFrameTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SignalEditFrame sef = new SignalEditFrame(
                "Edit Signal-1",
                null,
                null,
                null);
        Assert.assertNotNull("New SEF exists", sef);
    }

    @Test
    public void testCTorSignal() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        VirtualSignalMast m = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "mast1");
        TableFrames tf = new TableFrames();
        SignalTableModel model = new SignalTableModel(tf);
        Portal p1 = new Portal("OP1");
        OBlock b1 = new OBlock("OB1");
        OBlock b2 = new OBlock("OB2");
        SignalTableModel.SignalRow sr = new SignalTableModel.SignalRow(m, b1, p1, b2, 0.0f, false);

        SignalEditFrame sef = new SignalEditFrame("Edit mast1", m, sr, model);
        Assert.assertNotNull("Mast SEF exists", sef);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SignalEditFrameTest.class);

}
