package jmri.jmrit.beantable;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RailComTableActionTest extends AbstractTableActionBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", a);
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleRailComTable");
    }

    @Override
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("RailCom Table Action class description", "RailCom Locos", a.getClassDescription());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();        a = new RailComTableAction();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RailComTableActionTest.class);
}
