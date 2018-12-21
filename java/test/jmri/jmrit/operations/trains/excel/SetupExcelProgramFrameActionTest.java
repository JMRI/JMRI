package jmri.jmrit.operations.trains.excel;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SetupExcelProgramFrameActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SetupExcelProgramFrameAction t = new SetupExcelProgramFrameAction("test action");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SetupExcelProgramFrameActionTest.class);

}
