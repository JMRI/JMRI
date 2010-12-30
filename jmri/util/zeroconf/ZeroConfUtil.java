// ZeroConfUtil.java

package jmri.util.zeroconf;

import javax.jmdns.*;
import java.io.*;

/**
 *	Utilities for ZeroConf/Bonjour networking
 *
 *	@author Brett Hoffman   Copyright (C) 2009
 *	@author Bob Jacobsen    Copyright (C) 2009
 *	@version $Revision: 1.3 $
 */

public class ZeroConfUtil {

    // static final ResourceBundle rb = ResourceBundle.getBundle("jmri.util.zeroconf.ZeroConfBundle");

    /**
     * Provide a server name.
     * <p>
     * Attempts to get and use the local host name
     * <P>
     * Cannot contain periods ("."), so those are removed
     * along with anything after them.
     */
    static public String getServerName(String defaultName) {
        String serverName;
        try {
            serverName = java.net.InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            serverName = defaultName;
        }

        //	Name string of ServiceInfo cannot have a '.' in it.
        int dotIndex = serverName.indexOf('.');
        if (dotIndex == -1) {	//	Has no dot
            return serverName;
        } else if (dotIndex > 0) {	//	Has a dot, name will be up to dot
            return serverName.substring(0, dotIndex);
        } else {	//	Give up, assign generic name
            String genericName = "JMRI Server";
            log.warn("Setting default name \""+genericName+"\" for service discovery");
            return genericName;
        }
    }
    
    /**
     * Make a JmDNS instance
     */
    static public JmDNS jmdnsInstance() {
        if (jmdns == null) {
            try{
                jmdns = JmDNS.create();
            } catch (IOException e){
                log.error("JmDNS creation failed : "+e.getMessage());
                return null;
            }
        }
        return jmdns;
    }

    static JmDNS jmdns = null;
    
    /**
     * Advertise a zeroconf service.
     * <p>
     * Typical use:<br>
     * ZeroConfUtil.advertiseService(ZeroConfUtil.getServerName("WiThrottle"), "_withrottle._tcp.local.", port, ZeroConfUtil.jmdnsInstance());
     */
    static public ServiceInfo advertiseService(String serverName, String service, int port, JmDNS jmdns) throws IOException {
    		if (jmdns == null) {
    			log.warn("JmDNS not created.");
    			return null;
    		}
            ServiceInfo serviceInfo = ServiceInfo.create(service,
                                            serverName,
                                            port,
                                            "path=index.html");
            jmdns.registerService(serviceInfo);
            return serviceInfo;
        }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZeroConfUtil.class.getName());
}
