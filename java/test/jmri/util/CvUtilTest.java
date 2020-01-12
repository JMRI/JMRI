/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.util;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;

/**
 * Test CV Utilities
 *
 * @author Dave Heap Copyright (C) 2016
 */
public class CvUtilTest {

    public CvUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
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

    /**
     * Test of expandCvList method, of class CvUtil.
     */
    @Test
    public void testExpandCvList() {
//        System.out.println("expandCvList");
        String cvString;
        List<String> expResult;
        List<String> result;

        cvString = "abc";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "a(bc";
        expResult = new ArrayList<>(Arrays.asList("abc"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "ab)c";
        expResult = new ArrayList<>(Arrays.asList("abc"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "a(b)c";
        expResult = new ArrayList<>(Arrays.asList("abc"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "127";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "CV0.255.256";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.250:4";
        expResult = new ArrayList<>(Arrays.asList("25.3.250", "25.3.251", "25.3.252", "25.3.253"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "98-103";
        expResult = new ArrayList<>(Arrays.asList("98", "99", "100", "101", "102", "103"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.2(5:4)0";
        expResult = new ArrayList<>(Arrays.asList("25.3.250", "25.3.260", "25.3.270", "25.3.280"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3(1-7).25";
        expResult = new ArrayList<>(Arrays.asList("16.31.25", "16.32.25", "16.33.25", "16.34.25", "16.35.25", "16.36.25", "16.37.25"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "1,5,7";
        expResult = new ArrayList<>(Arrays.asList("1", "5", "7"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3.25(1,2,5,7)";
        expResult = new ArrayList<>(Arrays.asList("16.3.251", "16.3.252", "16.3.255", "16.3.257"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3.25(1-7)";
        expResult = new ArrayList<>(Arrays.asList("16.3.251", "16.3.252", "16.3.253", "16.3.254", "16.3.255", "16.3.256", "16.3.257"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3-7.254";
        expResult = new ArrayList<>(Arrays.asList("16.3.254", "16.4.254", "16.5.254", "16.6.254", "16.7.254"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "T3CV3-7.254";
        expResult = new ArrayList<>(Arrays.asList("T3CV3.254", "T3CV4.254", "T3CV5.254", "T3CV6.254", "T3CV7.254"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.7-0.254";
        expResult = new ArrayList<>(Arrays.asList("16.7.254", "16.6.254", "16.5.254", "16.4.254", "16.3.254", "16.2.254", "16.1.254", "16.0.254"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.23-20.254";
        expResult = new ArrayList<>(Arrays.asList("16.23.254", "16.22.254", "16.21.254", "16.20.254"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3.25(4-0)";
        expResult = new ArrayList<>(Arrays.asList("16.3.254", "16.3.253", "16.3.252", "16.3.251", "16.3.250"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "16.3.2(53-48)";
        expResult = new ArrayList<>(Arrays.asList("16.3.253", "16.3.252", "16.3.251", "16.3.250", "16.3.249", "16.3.248"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "T3CV25(4-0).5";
        expResult = new ArrayList<>(Arrays.asList("T3CV254.5", "T3CV253.5", "T3CV252.5", "T3CV251.5", "T3CV250.5"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.250:1";
        expResult = new ArrayList<>(Arrays.asList("25.3.250"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.250:-1";
        expResult = new ArrayList<>(Arrays.asList("25.3.250"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.250:0";
        expResult = new ArrayList<>(Arrays.asList("25.3.250"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3.250:-4";
        expResult = new ArrayList<>(Arrays.asList("25.3.250", "25.3.249", "25.3.248", "25.3.247"));
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);

        cvString = "25.3-4.250:4";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Invalid string '" + cvString + "'");

        cvString = "a(b(c)d)e";
        expResult = new ArrayList<>(Arrays.asList());
        result = CvUtil.expandCvList(cvString);
        Assert.assertEquals(expResult, result);
        // make sure an error message is generated.
        jmri.util.JUnitAppender.assertErrorMessage("Invalid string '" + cvString + "'");
    }

    /**
     * Test of addCvDescription method, of class CvUtil.
     */
    @Test
    public void testAddCvDescription() {
//        System.out.println("addCvDescription");
        String toolTip = "A test tooltip";
        String cvDescription = "CV999";
        String mask1 = "XVXVXVVV";
        String mask2 = "VVVVVVVV";
        String expResult;
        String result;

        // test with null toolTip
        result = CvUtil.addCvDescription(null, cvDescription, mask1);
        expResult = "CV999 bits 0-2,4,6";
        Assert.assertEquals(expResult, result);

        // test with empty toolTip
        result = CvUtil.addCvDescription("", cvDescription, mask1);
        expResult = "CV999 bits 0-2,4,6";
        Assert.assertEquals(expResult, result);

        // test with no mask
        result = CvUtil.addCvDescription(toolTip, cvDescription, mask2);
        expResult = "A test tooltip (CV999)";
        Assert.assertEquals(expResult, result);

        // test with no HTML
        result = CvUtil.addCvDescription(toolTip, cvDescription, mask1);
        expResult = "A test tooltip (CV999 bits 0-2,4,6)";
        Assert.assertEquals(expResult, result);

        // test with HTML
        result = CvUtil.addCvDescription("<html>" + toolTip + "</html>", cvDescription, mask1);
        expResult = "<html>A test tooltip (CV999 bits 0-2,4,6)</html>";
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getMaskDescription method, of class CvUtil.
     */
    @Test
    public void testGetMaskDescription() {
//        System.out.println("getMaskDescription");
        Assert.assertEquals("bit 0", CvUtil.getMaskDescription("XXXXXXXV"));
        Assert.assertEquals("bit 2", CvUtil.getMaskDescription("XXXXXVXX"));
        Assert.assertEquals("bits 0-2,4,6", CvUtil.getMaskDescription("XVXVXVVV"));
        Assert.assertEquals("bits 1-2,4-5", CvUtil.getMaskDescription("XXVVXVVX"));
        Assert.assertEquals("no bits", CvUtil.getMaskDescription("XXXXXXXX"));
    }

}
