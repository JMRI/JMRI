package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleAnalogExpressionSocketFactory.class)
public class DebuggerMaleAnalogExpressionSocketFactory implements MaleAnalogExpressionSocketFactory {

    @Override
    public MaleAnalogExpressionSocket encapsulateMaleSocket(BaseManager<MaleAnalogExpressionSocket> manager, MaleAnalogExpressionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleAnalogExpressionSocket(manager, maleSocket);
    }
    
}
