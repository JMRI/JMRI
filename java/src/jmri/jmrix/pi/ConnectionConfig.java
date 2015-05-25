// ConnectionConfig.java

package jmri.jmrix.pi;

/**
 * Handle configuring a Raspberry Pi layout connection.
 * <P>
 * This uses the {@link RaspberryPiAdapter} class to do the actual
 * connection.
 *
 * @author      Paul Bender  Copyright (C) 2015
 * @version	$Revision$
 *
 * @see RaspberryPiAdapter
 */
public class ConnectionConfig  extends jmri.jmrix.AbstractConnectionConfig {

    private boolean disabled = false;
    private RaspberryPiAdapter adapter = null;

    /**
     * Ctor for an object being created during load process;
     * Swing init is deferred.
     */
    public ConnectionConfig(RaspberryPiAdapter p){
        super();
        adapter = p;
    }

    /**
     * Ctor for a functional Swing object with no prexisting adapter
     */
    public ConnectionConfig() {
        super();
        adapter = new RaspberryPiAdapter();
    }

    @Override
    protected void checkInitDone(){
    }

    @Override
    public void updateAdapter(){
    }

    @Override
    protected void showAdvancedItems(){
    }

    @Override
    public void loadDetails(final javax.swing.JPanel details){
    }

    @Override
    protected void setInstance(){
       if(adapter==null){
          adapter = new RaspberryPiAdapter();
       }
    }

    @Override
    public jmri.jmrix.PortAdapter getAdapter(){
      return adapter;
    }

    @Override
    public String getInfo() { return "GPIO"; }

    @Override
    public String getManufacturer(){
      return jmri.jmrix.DCCManufacturerList.PI;
    }

    @Override
    public void setManufacturer(String manufacturer){
    }

    @Override
    public String name(){
       return getConnectionName();
    }

    @Override
    public String getConnectionName(){
       return "Raspberry Pi GPIO";
    }

    @Override
    public boolean getDisabled(){
       return disabled;
    }

    @Override
    public void setDisabled(boolean disable){
       this.disabled=disable;
    }

   @Override
   public void dispose(){
   } 
}


