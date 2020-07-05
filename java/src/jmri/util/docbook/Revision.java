package jmri.util.docbook;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Memo class to remember a single revision.
 *
 * @author Bob Jacobsen Copyright (c) 2010
 */
@API(status = EXPERIMENTAL)
public class Revision {

    public int revnumber;
    public String date;
    public String authorinitials;
    public String revremark;
}
