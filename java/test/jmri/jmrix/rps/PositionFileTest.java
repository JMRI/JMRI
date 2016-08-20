package jmri.jmrix.rps;

import java.io.File;
import java.io.IOException;
import javax.vecmath.Point3d;
import jmri.util.FileUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;

/**
 * JUnit tests for the rps.PositionFile class.
 * <P>
 * Stores a PositionFileTest.xml file in the temp directory below current
 * working directory.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class PositionFileTest extends TestCase {

    public void testCtor() {
        new PositionFile();
    }

    public void testPointElement() {
        PositionFile f = new PositionFile();

        Point3d in = new Point3d(-1.0f, -2.0f, -3.0f);
        Element e = f.positionElement(in);
        Point3d out = f.positionFromElement(e);
        checkPoint3d(in, out);
    }

    public void testReadingElement() {
        PositionFile f = new PositionFile();

        Reading in = new Reading("21", new double[]{0., 11, 12, 13, 14});

        Element e = f.readingElement(in);

        Reading out = f.readingFromElement(e);

        checkReading(in, out);
    }

    void checkPoint3d(Point3d first, Point3d second) {
        Assert.assertTrue("x ok", Math.abs(first.x - second.x) < 0.001);
        Assert.assertTrue("y ok", Math.abs(first.y - second.y) < 0.001);
        Assert.assertTrue("z ok", Math.abs(first.z - second.z) < 0.001);
    }

    void checkReading(Reading first, Reading second) {
        Assert.assertEquals("id ok", first.getID(), second.getID());
        Assert.assertEquals("num ok", first.getNValues(), second.getNValues());

        for (int i = 1; i <= first.getNValues(); i++) {
            Assert.assertTrue("" + i + " ok", Math.abs(first.getValue(i) - second.getValue(i)) < 0.001);
        }
    }

    public void testRW() throws IOException, org.jdom2.JDOMException {
        PositionFile fout = new PositionFile();
        fout.prepare();
        fout.setReceiver(2, new Point3d(1.0f, 2.0f, 3.0f), true);

        Reading rout = new Reading("21", new double[]{11, 12, 13, 14});
        fout.setCalibrationPoint(new Point3d(-1.0f, -2.0f, -3.0f), rout);

        FileUtil.createDirectory("temp");
        fout.store(new File("temp" + File.separator + "PositionFileTest.xml"));

        PositionFile fin = new PositionFile();
        fin.loadFile(new File("temp" + File.separator + "PositionFileTest.xml"));

        Point3d p;
        p = fin.getReceiverPosition(2);
        checkPoint3d(new Point3d(1.0f, 2.0f, 3.0f), p);

        Assert.assertEquals("no 2nd receiver position", null, fin.getReceiverPosition(3));

        p = fin.getCalibrationPosition(0);
        checkPoint3d(new Point3d(-1.0f, -2.0f, -3.0f), p);

        Assert.assertEquals("no 2nd calibration position", null, fin.getCalibrationPosition(1));

        Reading rin = fin.getCalibrationReading(0);
        checkReading(rout, rin);

        Assert.assertEquals("no 2nd calibration reading", null, fin.getCalibrationReading(1));
    }

    // from here down is testing infrastructure
    public PositionFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PositionFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PositionFileTest.class);
        return suite;
    }

}
