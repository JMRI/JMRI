package jmri.jmrix.zimo;

import jmri.JmriException;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Bob Jacobsen Copyright (C) 2001
 *
 */
@API(status = EXPERIMENTAL)
public class Mx1Exception extends JmriException {

    public Mx1Exception(String m) {

        super(m);

    }

}
