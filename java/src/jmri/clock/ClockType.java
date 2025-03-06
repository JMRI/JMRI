package jmri.clock;

/**
 * A clock type.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockType {

    /**
     * Set the Start Clock type Option.
     * @param option Clock type, e.g. NIXIE_CLOCK or PRAGOTRON_CLOCK
     */
    void setStartClockOption(int option);

    /**
     * Get the Start Clock Type.
     * @return Clock type, e.g. NIXIE_CLOCK or PRAGOTRON_CLOCK
     */
    int getStartClockOption();

    /**
     * Clock start option.
     * No startup clock type.
     */
    int NONE = 0x00;
    /**
     * Clock start option.
     * Startup Nixie clock type.
     */
    int NIXIE_CLOCK = 0x01;
    /**
     * Clock start option.
     * Startup Analogue clock type.
     */
    int ANALOG_CLOCK = 0x02;
    /**
     * Clock start option.
     * Startup LCD clock type.
     */
    int LCD_CLOCK = 0x04;
    /**
     * Clock start option.
     * Startup Pragotron clock type.
     */
    int PRAGOTRON_CLOCK = 0x08;

}
