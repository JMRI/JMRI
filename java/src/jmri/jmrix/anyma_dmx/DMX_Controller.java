package jmri.jmrix.anyma_dmx;

public interface DMX_Controller {

//    public void unexportAll();
//    public void removeAllListeners();
//    public void removeAllTriggers();

    public boolean isShutdown();

    public void shutdown();
}
