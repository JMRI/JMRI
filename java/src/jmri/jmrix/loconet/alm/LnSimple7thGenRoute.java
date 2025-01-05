package jmri.jmrix.loconet.alm;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author B. Milhaupt (C) 2024
 */
public class LnSimple7thGenRoute {
    private LnSimpleRouteEntry[] routeEntries;

    public LnSimple7thGenRoute() {
        this.routeEntries = new LnSimpleRouteEntry[8];
        for(int i=0; i < 8; ++i) {
            this.routeEntries[i] = new LnSimpleRouteEntry();
        }
    }

    /**
     * Getter.
     * @return the routeEntries
     */
    public LnSimpleRouteEntry[] getRouteEntries() {
        return this.routeEntries.clone();
    }

    /**
     * Setter.
     * @param routeEntries the routeEntries to set
     */
    public void setRouteEntries(LnSimpleRouteEntry[] routeEntries) {
        this.routeEntries = routeEntries.clone();
    }

    /**
     * Setter.
     * @param entryNum the entry to set
     * @param routeEntry the routeEntry to set
     */
    public void setRouteEntry(int entryNum, LnSimpleRouteEntry routeEntry) {
        this.routeEntries[entryNum] = routeEntry;
    }

    /**
     * Getter.
     * @param entryNum the entry to get
     * @return the route entries
     */
    public LnSimpleRouteEntry getRouteEntry(int entryNum) {
        return this.routeEntries[entryNum];
    }

//    private final static Logger log = LoggerFactory.getLogger(LnSimple7thGenRoute.class);
}
