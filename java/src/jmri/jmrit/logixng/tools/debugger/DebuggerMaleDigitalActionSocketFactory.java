package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleDigitalActionSocketFactory.class)
public class DebuggerMaleDigitalActionSocketFactory implements MaleDigitalActionSocketFactory {

    @Override
    public MaleDigitalActionSocket encapsulateMaleSocket(BaseManager<MaleDigitalActionSocket> manager, MaleDigitalActionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleDigitalActionSocket(manager, maleSocket);
    }
    
}
