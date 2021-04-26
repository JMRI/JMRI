package jmri.util.iharder.dnd;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.net.URI;

import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileDropEventTest {

    @Test
    public void testCTor(@TempDir File folder) throws java.io.IOException  {
        URI fl[] = new URI[3];
        File file = new File(folder, "1");
        file.createNewFile();
        fl[0]=file.toURI();
        file = new File(folder, "2");
        file.createNewFile();
        fl[1]=file.toURI();
        file = new File(folder, "3");
        file.createNewFile();
        fl[2]=file.toURI();
        URIDropEvent t = new URIDropEvent(fl,this);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileDropEventTest.class.getName());

}
