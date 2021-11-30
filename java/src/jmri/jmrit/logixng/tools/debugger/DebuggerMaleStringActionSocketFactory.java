package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleStringActionSocketFactory.class)
public class DebuggerMaleStringActionSocketFactory implements MaleStringActionSocketFactory {

    @Override
    public MaleStringActionSocket encapsulateMaleSocket(BaseManager<MaleStringActionSocket> manager, MaleStringActionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleStringActionSocket(manager, maleSocket);
    }
    
}
