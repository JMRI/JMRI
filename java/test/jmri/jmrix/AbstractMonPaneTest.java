package jmri.jmrix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractMonPaneTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Before
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

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractMonPaneTest.class);

}
