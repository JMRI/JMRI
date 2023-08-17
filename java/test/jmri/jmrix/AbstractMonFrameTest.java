package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true" )
public class AbstractMonFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        frame = new AbstractMonFrame(){
            @Override
            public String title(){
                return "test";
            }
            @Override
            public void init(){
            }
       };
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractMonFrameTest.class);

}
