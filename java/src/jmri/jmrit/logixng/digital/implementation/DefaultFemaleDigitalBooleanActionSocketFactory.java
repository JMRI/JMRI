package jmri.jmrit.logixng.digital.implementation;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketFactory;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;

/**
 *
 */
public class DefaultFemaleDigitalBooleanActionSocketFactory implements FemaleSocketFactory {

    @Override
    public FemaleSocket create(Base parent, FemaleSocketListener listener) {
        return InstanceManager.getDefault(DigitalBooleanActionManager.class).createFemaleSocket(parent, listener, getNewSocketName(parent));
    }

    @Override
    public MaleSocket getBeanBySystemName(String systemName) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getNewSocketName(Base parent) {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < parent.getChildCount(); i++) {
                String name = "A" + Integer.toString(x);
                if (name.equals(parent.getChild(i).getName())) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
                return "A" + Integer.toString(x);
            }
            x++;
        }
        throw new RuntimeException("Unable to find a new socket name");
    }

}
