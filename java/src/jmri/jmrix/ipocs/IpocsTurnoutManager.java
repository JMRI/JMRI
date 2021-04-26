package jmri.jmrix.ipocs;

import jmri.SystemConnectionMemo;
import jmri.Turnout;
import jmri.managers.AbstractTurnoutManager;

import javax.annotation.Nonnull;

/**
 *
 * @author Fredrik Elestedt Copyright (C) 2020
 * @since 4.21.2
 */
public class IpocsTurnoutManager extends AbstractTurnoutManager {

    public IpocsTurnoutManager(SystemConnectionMemo memo) {
        super(memo);
    }

    private IpocsPortController getPortController() {
        return ((IpocsSystemConnectionMemo)memo).getPortController();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
      return new IpocsTurnout(getPortController(), systemName, userName);
    }
}
