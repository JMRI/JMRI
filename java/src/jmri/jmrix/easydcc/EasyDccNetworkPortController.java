package jmri.jmrix.easydcc;

import jmri.jmrix.SystemConnectionMemo;

/**
 * Abstract base for classes representing an EasyDCC communications port.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public abstract class EasyDccNetworkPortController extends jmri.jmrix.AbstractNetworkPortController {

    /**
     * Base class. Implementations will provide InputStream and OutputStream
     * objects to EasyDccTrafficController classes, who in turn will deal in messages.
     *
     * @param connectionMemo associated memo for this connection
     */
    protected EasyDccNetworkPortController(SystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(EasyDccConnectionTypeList.EASYDCC);
    }

    @Override
    public EasyDccSystemConnectionMemo getSystemConnectionMemo() {
        return (EasyDccSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}
