package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ItemDialogTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ItemDialog("Sensors", "Icons", null);
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ItemDialogTest.class);

}
