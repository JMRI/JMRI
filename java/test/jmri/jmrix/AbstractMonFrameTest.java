package jmri.jmrix;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractMonFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        if(!GraphicsEnvironment.isHeadless()){
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
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractMonFrameTest.class);

}
