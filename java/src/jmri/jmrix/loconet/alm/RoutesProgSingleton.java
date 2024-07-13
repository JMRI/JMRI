package jmri.jmrix.loconet.alm;

/**
 * Singleton used to allow, at most, one Digitrax 7th-generation Accessory
 * Decoder "routes programmer" being instantiated.
 *
 * @author B. Milhaupt (C) 2024
 */
public enum RoutesProgSingleton {
    INSTANCE;

    private boolean devBeingProgForRoutes = false;

    public synchronized void setRoutesProgrammingActive(boolean t) {
        devBeingProgForRoutes = t;
    }
    public synchronized boolean getRoutesProgrammingActive() {
        return devBeingProgForRoutes;
    }
}
