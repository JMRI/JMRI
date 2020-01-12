package jmri.jmrix.loconet.sdf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.sdf.SdfBuffer class.
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class SdfBufferTest {

    @Test
    public void testFileCtor() throws java.io.IOException {
        SdfBuffer b = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");

        String result = b.toString();

        // read the golden file
        String g = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader("java/test/jmri/jmrix/loconet/sdf/test2.golden.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                g += (str + "\n");
            }
            in.close();
        } catch (IOException e) {
            log.error("exception reading golden file: " + e);
            System.out.println("exception reading golden file: " + e);
        }

        if (!result.equals(g)) {
            // The next lines prints the answer in case you need
            // to create a new golden file
            System.out.println("--------------------");
            System.out.println(result);
            System.out.println("--------------------");
        }

        Assert.assertEquals("output as string", g, result);
    }

    @Test
    public void testModify() throws java.io.IOException {
        // get original file
        SdfBuffer original = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        byte[] oarray = original.getByteArray();

        // and a version to modify
        SdfBuffer b = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        Assert.assertEquals("original lengths", oarray.length, b.getByteArray().length);

        // modify the 1st SDF     
        SkemeStart first = (SkemeStart) b.getMacroList().get(0);
        int startlength = first.getNumber();
        first.setNumber(23);

        // recreate buffer; expect same length, different contents in the 1st byte
        b.loadByteArray();

        byte barray[];

        barray = b.getByteArray();
        Assert.assertEquals("updated lengths", oarray.length, barray.length);
        Assert.assertTrue("modified 1st byte same", oarray[0] == barray[0]);
        Assert.assertTrue("modified 2nd byte differ", oarray[1] != barray[1]);
        for (int i = 2; i < barray.length; i++) {
            if (oarray[i] != barray[i]) {
                Assert.fail("modified failed to match at index " + i);
            }
        }

        // set it back, and make sure length and content is the same
        first.setNumber(startlength);
        b.loadByteArray();

        barray = b.getByteArray();
        Assert.assertEquals("last lengths", oarray.length, barray.length);
        Assert.assertTrue("last 1st byte same", oarray[0] == barray[0]);
        Assert.assertTrue("last 2nd byte same", oarray[1] == barray[1]);
        for (int i = 2; i < barray.length; i++) {
            if (oarray[i] != barray[i]) {
                Assert.fail("last failed to match at index " + i);
            }
        }

    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SdfBufferTest.class);

}
