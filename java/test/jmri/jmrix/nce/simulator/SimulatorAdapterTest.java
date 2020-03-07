package jmri.jmrix.nce.simulator;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SimulatorAdapterTest {
        
    private SimulatorAdapter adapter;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",adapter);
    }

    @Test
    public void testOpenPort() {
        Assert.assertFalse("status before open",adapter.status());
        Assert.assertNull("InputStream Before Open",adapter.getInputStream());
        jmri.util.JUnitAppender.assertErrorMessage("getInputStream called before load(), stream not available");
        Assert.assertNull("OutputStream Before Open",adapter.getOutputStream());
        jmri.util.JUnitAppender.assertErrorMessage("getOutputStream called before load(), stream not available");
        Assert.assertNull("port opens",adapter.openPort("test","test"));
        Assert.assertTrue("status after open",adapter.status());
        Assert.assertNotNull("InputStream After Open",adapter.getInputStream());
        Assert.assertNotNull("OutputStream After Open",adapter.getOutputStream());
    }

    @Test
    public void testBaudRates(){
       Assert.assertEquals("empty baud rates array for simulator", new String[]{}, adapter.validBaudRates());
       Assert.assertEquals("no currentbaudrate set for simulator","", adapter.getCurrentBaudRate());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        adapter = new SimulatorAdapter();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        adapter.dispose();
        adapter = null;
    }

    // private final static Logger log = LoggerFactory.getLogger(SimulatorAdapterTest.class);

}
