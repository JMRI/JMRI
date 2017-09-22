package jmri.jmrit.signalling.entryexit;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SourceTest {

    @Test
    @Ignore("needs more setup")
    public void testCTor() {
        LayoutBlock f = new LayoutBlock("test1","Facing Block");
        LayoutBlock p1 = new LayoutBlock("test2","Protecting Block 1");
        LayoutBlock p2 = new LayoutBlock("test3","Protecting Block 2");
        List<LayoutBlock> blockList = new ArrayList<>();
        blockList.add(p1);
        blockList.add(p2);
        PointDetails ptd = new PointDetails(f,blockList);
        jmri.SignalMast sm = new jmri.implementation.VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        ptd.setSignalMast(sm);
        Source t = new Source(ptd);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SourceTest.class);

}
