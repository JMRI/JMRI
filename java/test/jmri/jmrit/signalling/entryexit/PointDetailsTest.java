package jmri.jmrit.signalling.entryexit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PointDetailsTest {

    @Test
    public void testCTor() {
        LayoutBlock f = new LayoutBlock("test1","Facing Block");
        LayoutBlock p1 = new LayoutBlock("test2","Protecting Block 1");
        LayoutBlock p2 = new LayoutBlock("test3","Protecting Block 2");
        List<LayoutBlock> blockList = new ArrayList<LayoutBlock>();
        blockList.add(p1);
        blockList.add(p2);
        PointDetails t = new PointDetails(f,blockList);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PointDetailsTest.class.getName());

}
