package jmri.jmrix.ecos;

/**
 * Identifying class representing a ECOS communications port
 *
 * @author   Bob Jacobsen    Copyright (C) 2001, 2008
 */
public abstract class EcosPortController extends jmri.jmrix.AbstractNetworkPortController {

    // base class. Implementations will provide InputStream and OutputStream
    // objects to EcosTrafficController classes, who in turn will deal in messages.
    protected EcosPortController(EcosSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
    }

    @Override
    public EcosSystemConnectionMemo getSystemConnectionMemo() {
        return (EcosSystemConnectionMemo) super.getSystemConnectionMemo();
    }

}
