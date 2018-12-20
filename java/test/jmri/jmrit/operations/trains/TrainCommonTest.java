package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the TrainCommon class 
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class TrainCommonTest extends OperationsTestCase {

    @Test
    public void testGetDate_DateArgument(){
       java.util.Calendar calendar = java.util.Calendar.getInstance();
       String date = TrainCommon.getDate(calendar.getTime());
       Assert.assertNotNull("Date String",date);
    }

    @Test
    public void testGetDate_BooleanArgument(){
       String date = TrainCommon.getDate(false);
       Assert.assertNotNull("Date String",date);
    }
}
