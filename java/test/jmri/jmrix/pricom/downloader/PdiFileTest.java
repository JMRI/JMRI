package jmri.jmrix.pricom.downloader;

import org.junit.Test;
import org.junit.Assert;

/**
 * JUnit tests for the PdiFile class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class PdiFileTest {

    @Test
    public void testCreate() {
        new PdiFile(null);
    }

    // create and show, with some data present
    @Test
    public void testOpen() {
        PdiFile f = new PdiFile(null);
        Assert.assertNotNull("exists", f);
    }
}
