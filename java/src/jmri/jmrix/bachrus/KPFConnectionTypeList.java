package jmri.jmrix.bachrus;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 * Returns a list of valid KPF-Zeller Connection Types
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2022
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Andrew Crosland Copyright (C) 2010
  *
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class KPFConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String KPFZELLER = "KPF-Zeller";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.bachrus.kpfserialdriver.ConnectionConfig"
        };
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{KPFZELLER};
    }

}
