package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Adapter to allow the system connection memo and multiple
 * RaspberryPi managers to be handled.
 * <P>
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @author			Paul Bender Copyright (C) 2015
 */
public class RaspberryPiAdapter extends jmri.jmrix.AbstractPortController
    implements jmri.jmrix.PortAdapter{

    // private control members
    private boolean opened = false;
    // in theory gpio can be static, because there will only ever
    // be one, but the library handles the details that make it a 
    // singleton.
    private GpioController gpio = null;

    public RaspberryPiAdapter(){
        this(GpioFactory.getInstance());
    }
    
    public RaspberryPiAdapter(GpioController _gpio){
        super(new RaspberryPiSystemConnectionMemo());
        log.debug("RaspberryPi GPIO Adapter Constructor called");
        opened = true;
        this.manufacturerName = RaspberryPiConnectionTypeList.PI;
        gpio = _gpio;
    }

    @Override
    public String getCurrentPortName(){ return "GPIO"; }

    @Override
    public void dispose() {
        super.dispose();
        gpio.shutdown(); // terminate all GPIO connections.
    }

   @Override
   public void connect(){
   }

   @Override
   public void configure() {
      this.getSystemConnectionMemo().configureManagers();
   }

   @Override
   public boolean status() {
	return opened;
   }

   @Override
   public java.io.DataInputStream getInputStream() {
       return null;
   }

   @Override
   public java.io.DataOutputStream getOutputStream() {
       return null;
   }
    
   @Override
   public RaspberryPiSystemConnectionMemo getSystemConnectionMemo() { 
      return (RaspberryPiSystemConnectionMemo) super.getSystemConnectionMemo(); 
   }

   @Override
   public void recover(){
   }

   /*
    * get the GPIO Controller associated with this object.
    *
    * @return GpioController object.
    */
   public GpioController getGPIOController(){ return gpio; }

   private final static Logger log = LoggerFactory.getLogger(RaspberryPiAdapter.class.getName());

}
