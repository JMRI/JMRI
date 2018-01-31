package jmri.util.iharder.dnd;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import java.io.File;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class FileDropEventTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCTor() throws java.io.IOException  {
        File fl[] = new File[3];
        fl[0]=folder.newFile();
        fl[1]=folder.newFile();
        fl[2]=folder.newFile();
        FileDropEvent t = new FileDropEvent(fl,this);
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

    // private final static Logger log = LoggerFactory.getLogger(FileDropEventTest.class.getName());

}
