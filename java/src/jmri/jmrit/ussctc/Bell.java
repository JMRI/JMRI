package jmri.jmrit.ussctc;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface defining operations for a CTC machine bell.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
@API(status = MAINTAINED)
public interface Bell {

    public void ring();

}
