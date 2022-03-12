package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleAnalogActionSocketFactory.class)
public class DebuggerMaleAnalogActionSocketFactory implements MaleAnalogActionSocketFactory {

    @Override
    public MaleAnalogActionSocket encapsulateMaleSocket(BaseManager<MaleAnalogActionSocket> manager, MaleAnalogActionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleAnalogActionSocket(manager, maleSocket);
    }
    
}
