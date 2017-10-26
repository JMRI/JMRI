package jmri.jmrix.anyma;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid anyma Connection Types.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author George Warner Copyright (C) 2017
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class AnymaConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String ANYMA = "anyma";

    @Override
    public String[] getAvailableProtocolClasses() {
        String[] masterList = new jmri.jmrix.lenz.LenzConnectionTypeList().getAvailableProtocolClasses();

        String[] tempList = new String[masterList.length + 1];
        tempList[0] = "jmri.jmrix.anyma.udmx.ConnectionConfig";
        int x = 1;
        for (int i = 0; i < masterList.length; i++) {
            tempList[x] = masterList[i];
            x++;
        }
        return tempList;
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{ANYMA};
    }

}
