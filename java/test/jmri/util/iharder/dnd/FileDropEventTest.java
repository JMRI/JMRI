package jmri.util.iharder.dnd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.net.URI;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FileDropEventTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() throws java.io.IOException  {
        URI fl[] = new URI[3];
        fl[0]=folder.newFile().toURI();
        fl[1]=folder.newFile().toURI();
        fl[2]=folder.newFile().toURI();
        URIDropEvent t = new URIDropEvent(fl,this);
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(FileDropEventTest.class.getName());

}
