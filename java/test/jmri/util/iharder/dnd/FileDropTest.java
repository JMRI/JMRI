package jmri.util.iharder.dnd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import javax.swing.JPanel;
import java.awt.GraphicsEnvironment;
import java.net.URI;

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
        JPanel myPanel = new JPanel();
        URIDrop t = new URIDrop(myPanel, new URIDrop.Listener() {
            @Override
            public void URIsDropped(URI[] uris) {
                // handle file drop
            }   // end filesDropped
        }); // end URIDrop.Listener

        Assert.assertNotNull("exists", t);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileDropTest.class.getName());
}
