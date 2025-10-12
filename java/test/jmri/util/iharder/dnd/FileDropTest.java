package jmri.util.iharder.dnd;

import javax.swing.JPanel;

import java.net.URI;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileDropTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() throws java.io.IOException {

        // this came was modifed from the FileDrop website's example at
        // http://iharder.sourceforge.net/current/java/filedrop/ 
        JPanel myPanel = new JPanel();
        URIDrop t = new URIDrop(myPanel, new URIDrop.Listener() {
            @Override
            public void URIsDropped(URI[] uris) {
                // handle file drop
            }   // end filesDropped
        }); // end URIDrop.Listener

        Assertions.assertNotNull( t, "exists");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileDropTest.class.getName());
}
