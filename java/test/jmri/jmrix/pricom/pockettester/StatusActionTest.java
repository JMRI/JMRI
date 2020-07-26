package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class StatusActionTest {

    @Test
    public void testCTor() {
        StatusAction t = new StatusAction(){
            @Override
            void connect(StatusFrame l){
            }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DataSourceActionTest.class);

}
