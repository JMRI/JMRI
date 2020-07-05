package jmri.jmrit.throttle;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author glen Copyright (C) 2002
 */
@API(status = MAINTAINED)
public interface ControlPanelListener extends java.util.EventListener {

    public void notifySpeedChanged(int speed);

    public void notifyDirectionChanged(boolean isForward);

}
