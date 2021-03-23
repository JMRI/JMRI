package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleDigitalBooleanActionSocketFactory.class)
public class DebuggerMaleDigitalBooleanActionSocketFactory implements MaleDigitalBooleanActionSocketFactory {

    @Override
    public MaleDigitalBooleanActionSocket encapsulateMaleSocket(BaseManager<MaleDigitalBooleanActionSocket> manager, MaleDigitalBooleanActionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleDigitalBooleanActionSocket(manager, maleSocket);
    }
    
}
