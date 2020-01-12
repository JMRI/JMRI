package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationEditFrame;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintLocationsByCarTypesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationEditFrame f = new LocationEditFrame(null);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Locations By Car Types");
        PrintLocationsByCarTypesAction t = new PrintLocationsByCarTypesAction("Test Action",jf,true,f);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(f);
        JUnitUtil.dispose(jf);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintLocationsByCarTypesActionTest.class);

}
