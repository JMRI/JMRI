package jmri.jmrit.withrottle;

import java.lang.reflect.Method;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.ThrottleListener;

/**
 *
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision: 1.2 $
 */
public class ConsistFunctionController extends AbstractController implements ThrottleListener{

    private DccLocoAddress address;
    private DccThrottle throttle;

    public ConsistFunctionController(DccLocoAddress address){
        this.address = address;

    }
    
    public void notifyThrottleFound(DccThrottle t) {
        log.debug("Lead Loco throttle found");
        throttle = t;
    }
    
    public void dispose(){
        throttle.release();
    }


    @Override
    boolean verifyCreation() {
        log.debug("verify creation");
        return jmri.InstanceManager.throttleManagerInstance().requestThrottle(address.getNumber(), address.isLongAddress(), this);
    }

    @Override
    void handleMessage(String inPackage){
        log.debug("handleMessage in CFC");
        //	get the function # sent from device
        String receivedFunction = inPackage.substring(2);
        Boolean state = false;

        if (inPackage.charAt(1) == '1'){	//	Function Button down
            if(log.isDebugEnabled()) log.debug("Trying to set function " + receivedFunction);
            //	Toggle button state:
            try{
                Method getF = throttle.getClass().getMethod("getF"+receivedFunction,(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = throttle.getClass().getMethod("setF"+receivedFunction, partypes);

                state = (Boolean)getF.invoke(throttle, (Object[])null);
                Object data[] = {new Boolean(!state)};

                setF.invoke(throttle, data);


            }catch (NoSuchMethodException ea){
                log.warn(ea);
            }catch (IllegalAccessException eb){
                log.warn(eb);
            }catch (java.lang.reflect.InvocationTargetException ec){
                log.warn(ec);
            }

        }else {	//	Function Button up

            //  F2 is momentary for horn
            //  Need to figure out what to do, Should this be in prefs?

            if (receivedFunction.equals("2")){
                throttle.setF2(false);
                return;
            }

            //	Do nothing if lockable, turn off if momentary
            try{
                Method getFMom = throttle.getClass().getMethod("getF"+receivedFunction+"Momentary",(Class[])null);

                Class partypes[] = {Boolean.TYPE};
                Method setF = throttle.getClass().getMethod("setF"+receivedFunction, partypes);

                if ((Boolean)getFMom.invoke(throttle, (Object[])null)){
                    Object data[] = {new Boolean(false)};

                    setF.invoke(throttle, data);
                }

            }catch (NoSuchMethodException ea){
                log.warn(ea);
            }catch (IllegalAccessException eb){
                log.warn(eb);
            }catch (java.lang.reflect.InvocationTargetException ec){
                log.warn(ec);
            }

        }

    }

    //  no list is needed
    public void checkCanBuildList(){}

    @Override
    void register() {}

    @Override
    void deregister() {}



    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConsistFunctionController.class.getName());

}
