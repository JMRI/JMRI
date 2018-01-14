package jmri.util.iharder.dnd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JPanel;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class FileDropTest {

    @Test
    public void testCTor() throws java.io.IOException {
      Assume.assumeFalse(GraphicsEnvironment.isHeadless());
      // this came was modifed from the FileDrop website's example at
      // http://iharder.sourceforge.net/current/java/filedrop/ 
      JPanel  myPanel = new JPanel();
      FileDrop t = new  FileDrop( myPanel, new FileDrop.Listener()
      {   public void  filesDropped( java.io.File[] files )
          {   
              // handle file drop
          }   // end filesDropped
      }); // end FileDrop.Listener

        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(FileDropTest.class.getName());

}
