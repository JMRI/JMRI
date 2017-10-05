package jmri.jmrix.maple;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum required SystemConnectionMemo for Maple.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class MapleSystemConnectionMemo extends SystemConnectionMemo {

    public MapleSystemConnectionMemo() {
        this("K", SerialConnectionTypeList.MAPLE);
    }

    public MapleSystemConnectionMemo(@Nonnull String prefix, @Nonnull String userName) {
        super(prefix, userName);

        register(); // registers general type
        InstanceManager.store(this, MapleSystemConnectionMemo.class); // also register as specific type

        log.debug("Created MapleSystemConnectionMemo");
    }

    private SerialTrafficController tc = null;

    /**
     * Set the traffic controller instance associated with this connection memo.
     *
     * @param s jmri.jmrix.maple.SerialTrafficController object to use.
     */
    public void setTrafficController(SerialTrafficController s){
        tc = s;
    }

    /**
     * Get the traffic controller instance associated with this connection memo.
     */
    public SerialTrafficController getTrafficController(){
        if (tc == null) {
            setTrafficController(new SerialTrafficController());
            log.debug("Auto create of SerialTrafficController for initial configuration");
        }
        return tc;
    }
    // would more stuff for traffic controller etc be useful here?

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(MapleSystemConnectionMemo.class);

}
