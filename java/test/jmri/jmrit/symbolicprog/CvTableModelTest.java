package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author	Bob Jacobsen
  */
public class CvTableModelTest {

    @Test
    public void testStart() {
        Assert.assertNotNull(new CvTableModel(new JLabel(), null));
    }

    // static Logger log = LoggerFactory.getLogger(CvTableModelTest.class.getName());
}
