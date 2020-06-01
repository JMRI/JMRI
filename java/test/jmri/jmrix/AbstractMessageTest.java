package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractMessageTest extends AbstractMessageTestBase {

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
