package jmri.jmrix.acela;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AcelaSignalHeadTest {

    private AcelaSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        AcelaSignalHead t = new AcelaSignalHead("AH1",memo);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitAppender.assertErrorMessage("Can't find new Acela Signal with name 'AH1'");
    }

    @Test
    public void test2stringCTor() {
        AcelaSignalHead t = new AcelaSignalHead("AH1","test",memo);
        Assert.assertNotNull("exists",t);
        jmri.util.JUnitAppender.assertErrorMessage("Can't find new Acela Signal with name 'AH1'");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        AcelaTrafficController tc = new AcelaTrafficControlScaffold();
        memo = new AcelaSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(AcelaSignalHeadTest.class);

}
