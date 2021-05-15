package jmri.jmrit.logixng.tools.debugger;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = MaleStringExpressionSocketFactory.class)
public class DebuggerMaleStringExpressionSocketFactory implements MaleStringExpressionSocketFactory {

    @Override
    public MaleStringExpressionSocket encapsulateMaleSocket(BaseManager<MaleStringExpressionSocket> manager, MaleStringExpressionSocket maleSocket) {
        
        if (! InstanceManager.getDefault(LogixNGPreferences.class).getInstallDebugger()) {
            return maleSocket;
        }
        
        return new DebuggerMaleStringExpressionSocket(manager, maleSocket);
    }
    
}
