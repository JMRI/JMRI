package jmri.jmrit.logixng.digital.implementation;

import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.FemaleSocketFactory;
import jmri.jmrit.logixng.FemaleSocketListener;

/**
 *
 */
public class DefaultFemaleDigitalExpressionSocketFactory implements FemaleSocketFactory {

    @Nonnull
    @Override
    public FemaleSocket create(@Nonnull Base parent, @Nonnull FemaleSocketListener listener) {
        return InstanceManager.getDefault(DigitalExpressionManager.class)
                .createFemaleSocket(parent, listener, getNewSocketName(parent));
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
