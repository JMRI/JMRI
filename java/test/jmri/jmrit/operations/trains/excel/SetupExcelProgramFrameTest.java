package jmri.jmrit.operations.trains.excel;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SetupExcelProgramFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SetupExcelProgramFrame t = new SetupExcelProgramFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramFrameTest.class);

}
