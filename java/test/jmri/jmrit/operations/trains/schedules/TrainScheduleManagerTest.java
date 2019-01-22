package jmri.jmrit.operations.trains.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainScheduleManagerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainScheduleManager t = new TrainScheduleManager();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testGetComboBox() {
        TrainScheduleManager t = new TrainScheduleManager();
        Assert.assertNotNull("ComboBox Available", t.getComboBox());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainScheduleManagerTest.class);

}
