package jmri.jmrit.entryexit;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ManuallySetRouteTest {

    @Test
    public void testCTor() {
        LayoutBlock f = new LayoutBlock("test1","Facing Block");  // NOI18N
        LayoutBlock p1 = new LayoutBlock("test2","Protecting Block 1");  // NOI18N
        LayoutBlock p2 = new LayoutBlock("test3","Protecting Block 2");  // NOI18N
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(p1);
        blockList.add(p2);
        PointDetails ptd = new PointDetails(f,blockList);
        jmri.SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");  // NOI18N
        ptd.setSignalMast(sm);
        ManuallySetRoute t = new ManuallySetRoute(ptd);
        Assert.assertNotNull("exists",t);  // NOI18N
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ManuallySetRouteTest.class);

}
