package jmri.jmrix.loconet.sdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
        StringBuilder g = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(
            "java/test/jmri/jmrix/loconet/sdf/test2.golden.txt"));){

            String str;
            while ((str = in.readLine()) != null) {
                g.append(str).append("\n");
            }
            in.close();
        } catch (IOException e) {
            fail("exception reading golden file: ", e);
        }

        if (!result.equals(g.toString())) {
            // The next lines prints the answer in case you need
            // to create a new golden file
            System.out.println("--------------------");
            System.out.println(result);
            System.out.println("--------------------");
        }

        assertEquals( g.toString(), result, "output as string");
    }

    @Test
    public void testModify() throws java.io.IOException {
        // get original file
        SdfBuffer original = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        byte[] oarray = original.getByteArray();

        // and a version to modify
        SdfBuffer b = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        assertEquals( oarray.length, b.getByteArray().length, "original lengths");

        // modify the 1st SDF     
        SkemeStart first = (SkemeStart) b.getMacroList().get(0);
        int startlength = first.getNumber();
        first.setNumber(23);

        // recreate buffer; expect same length, different contents in the 1st byte
        b.loadByteArray();

        byte barray[];

        barray = b.getByteArray();
        assertEquals( oarray.length, barray.length, "updated lengths");
        assertEquals( oarray[0], barray[0], "modified 1st byte same");
        assertNotEquals( oarray[1], barray[1], "modified 2nd byte differ");
        for (int i = 2; i < barray.length; i++) {
            assertEquals( oarray[i], barray[i],
                "modified failed to match at index " + i);
        }

        // set it back, and make sure length and content is the same
        first.setNumber(startlength);
        b.loadByteArray();

        barray = b.getByteArray();
        assertEquals( oarray.length, barray.length, "last lengths");
        assertEquals( oarray[0], barray[0], "last 1st byte same");
        assertEquals( oarray[1], barray[1], "last 2nd byte same");
        for (int i = 2; i < barray.length; i++) {
            assertEquals( oarray[i], barray[i],
                "last failed to match at index " + i);
        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SdfBufferTest.class);

}
