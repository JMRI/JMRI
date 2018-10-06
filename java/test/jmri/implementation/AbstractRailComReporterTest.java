package jmri.implementation;

import jmri.util.JUnitUtil;
import org.junit.*;
/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractRailComReporterTest extends AbstractReporterTestBase {

    @Override
    protected Object generateObjectToReport(){
        return new DefaultRailCom("ID1234", "Test Tag");
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        r = new AbstractRailComReporter("IR1");
    }

    @After
    @Override
    public void tearDown() {
        r = null;
        JUnitUtil.tearDown();
    }


}
