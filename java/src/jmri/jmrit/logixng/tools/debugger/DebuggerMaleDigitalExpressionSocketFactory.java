package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleDigitalExpressionSocketFactory.class)
public class DebuggerMaleDigitalExpressionSocketFactory implements MaleDigitalExpressionSocketFactory {

    @Override
    public MaleDigitalExpressionSocket encapsulateMaleSocket(BaseManager<MaleDigitalExpressionSocket> manager, MaleDigitalExpressionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleDigitalExpressionSocket(manager, maleSocket);
    }
    
}
