package jmri.jmrit.ussctc;

import jmri.*;
import jmri.jmrit.Sound;

/**
 * Derive a CTC machine bell from the computer's speakers
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017, 2021
 */
public class ComputerBell implements Bell {

    public ComputerBell(Sound sound) {
        this.sound = sound;
    }

    Sound sound = null;

    @Override
    public void ring() {
        sound.play();
    }

}
