package jmri.jmrix.cmri.serial;

import java.util.ArrayList;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * A List of of SerialNodes currently running, plus some utility functions
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
@API(status = EXPERIMENTAL)
public class SerialNodeList extends ArrayList<SerialNode> {

    public SerialNodeList() { super(); }
  
}
