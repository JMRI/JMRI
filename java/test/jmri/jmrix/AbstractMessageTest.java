package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractMessageTest extends AbstractMessageTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        m = new AbstractMessage(5){
            @Override
            public String toString(){
                 return "";
            }
        };
    }

    @After
    public void tearDown() {
	m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractMessageTest.class);

}
