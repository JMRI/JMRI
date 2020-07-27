package jmri.jmrix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractMonPaneTest extends jmri.util.swing.JmriPanelTest {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        panel = new AbstractMonPane(){
           @Override
           public String getTitle(){
              return "test";
           }
           @Override
           public void init(){
           }
        };
        title = "test";
        helpTarget = "package.jmri.jmrix.AbstractMonFrame";
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractMonPaneTest.class);

}
