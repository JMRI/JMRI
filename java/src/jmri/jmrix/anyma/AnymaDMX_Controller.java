package jmri.jmrix.anyma;

/**
 * Base for classes representing a Anyma DMX communications port
 *
 * @author George Warner Copyright (c) 2017
 * @since 4.9.6
 */

public interface AnymaDMX_Controller{

//    public void unexportAll();
//    public void removeAllListeners();
//    public void removeAllTriggers();

    public boolean isShutdown();

    public void shutdown();
}
