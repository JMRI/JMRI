package jmri.jmrit.logixng.tools.debugger;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Factory classes for the debugger male sockets.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class DebuggerMaleSocketFactory {
    
    public static void initializeDebugger() {
        if (1==1) throw new RuntimeException("Daniel");
        InstanceManager.getDefault(DigitalActionManager.class)
                .registerMaleSocketFactory(new DebuggerDigitalActionSocketFactory());
    }
    
    
    private static class DebuggerDigitalActionSocketFactory implements MaleSocketFactory<MaleDigitalActionSocket> {

        @Override
        public MaleDigitalActionSocket encapsulateMaleSocket(BaseManager<MaleDigitalActionSocket> manager, MaleDigitalActionSocket maleSocket) {
            if (1==1) throw new RuntimeException("Daniel");
            return new DebuggerMaleDigitalActionSocket(manager, maleSocket);
        }
        
    }
    
    
    
    
    
}
