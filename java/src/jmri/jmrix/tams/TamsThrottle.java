package jmri.jmrix.tams;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an TAMS connection.
 * <P>
 * Based on Glen Oberhauser's original LnThrottle implementation
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version $Revision: 19946 $
 */
public class TamsThrottle extends AbstractThrottle implements TamsListener {

    /**
     * Constructor.
     */
    public TamsThrottle(TamsSystemConnectionMemo memo, DccLocoAddress address) {
        super(memo);
        super.speedStepMode = SpeedStepMode128;
        tc = memo.getTrafficController();

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.address = address;
        this.isForward = true;

        //get the status if know of the current loco
        TamsMessage m = new TamsMessage("xL " + address.getNumber());
        m.setTimeout(10000);
        tc.sendTamsMessage(m, this);

        tc.addPollMessage(m, this);

        m = new TamsMessage("xF " + address.getNumber());
        tc.sendTamsMessage(m, this);
        tc.addPollMessage(m, this);

        m = new TamsMessage("xFX " + address.getNumber());
        tc.sendTamsMessage(m, this);
        tc.addPollMessage(m, this);

    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4. To
     * send function group 1 we have to also send speed, direction etc.
     */
    protected void sendFunctionGroup1() {

        StringBuilder sb = new StringBuilder();
        sb.append("xL ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(",");
        sb.append((f0 ? "1" : "0"));
        sb.append(",");
        sb.append(",");
        sb.append((f1 ? "1" : "0"));
        sb.append(",");
        sb.append((f2 ? "1" : "0"));
        sb.append(",");
        sb.append((f3 ? "1" : "0"));
        sb.append(",");
        sb.append((f4 ? "1" : "0"));
        TamsMessage m = new TamsMessage(sb.toString());
        tc.sendTamsMessage(m, this);
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     */
    protected void sendFunctionGroup2() {
        StringBuilder sb = new StringBuilder();
        sb.append("xF ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append((f5 ? "1" : "0"));
        sb.append(",");
        sb.append((f6 ? "1" : "0"));
        sb.append(",");
        sb.append((f7 ? "1" : "0"));
        sb.append(",");
        sb.append((f8 ? "1" : "0"));

        TamsMessage m = new TamsMessage(sb.toString());
        tc.sendTamsMessage(m, this);
    }

    protected void sendFunctionGroup3() {
        StringBuilder sb = new StringBuilder();
        sb.append("xFX ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append((f9 ? "1" : "0"));
        sb.append(",");
        sb.append((f10 ? "1" : "0"));
        sb.append(",");
        sb.append((f11 ? "1" : "0"));
        sb.append(",");
        sb.append((f12 ? "1" : "0"));

        TamsMessage m = new TamsMessage(sb.toString());
        tc.sendTamsMessage(m, this);

    }

    /**
     * Set the speed & direction.
     * <P>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    public void setSpeedSetting(float speed) {
        float oldSpeed = this.speedSetting;
        this.speedSetting = speed;

        int value = (int) ((127 - 1) * this.speedSetting);     // -1 for rescale to avoid estop
        if (value > 0) {
            value = value + 1;  // skip estop
        }
        if (value > 127) {
            value = 127;    // max possible speed
        }
        if (value < 0) {
            value = 1;        // emergency stop
        }
        StringBuilder sb = new StringBuilder();
        sb.append("xL ");
        sb.append(address.getNumber());
        sb.append(",");
        sb.append(value);
        sb.append(",");
        sb.append(",");
        sb.append((isForward ? "f" : "r"));
        sb.append(",");
        sb.append(",");
        sb.append(",");
        sb.append(",");

        TamsMessage m = new TamsMessage(sb.toString());
        tc.sendTamsMessage(m, this);

        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener("SpeedSetting", oldSpeed, this.speedSetting);
        }
    }

    public void setIsForward(boolean forward) {
        boolean old = isForward;
        isForward = forward;
        setSpeedSetting(speedSetting);  // send the command
        if (old != isForward) {
            notifyPropertyChangeListener("IsForward", old, isForward);
        }
    }

    private DccLocoAddress address;

    TamsTrafficController tc;

    public LocoAddress getLocoAddress() {
        return address;
    }

    protected void throttleDispose() {
        active = false;
        TamsMessage m = new TamsMessage("xL " + address.getNumber());
        tc.removePollMessage(m, this);

        m = new TamsMessage("xF " + address.getNumber());
        tc.removePollMessage(m, this);

        m = new TamsMessage("xFX " + address.getNumber());
        tc.removePollMessage(m, this);
    }

    public void message(TamsMessage m) {
        //System.out.println("Ecos message - "+ m);
        // messages are ignored
    }

    /**
     * Convert a Tams speed integer to a float speed value
     */
    protected float floatSpeed(int lSpeed) {
        if (lSpeed == 0) {
            return 0.f;
        } else if (lSpeed == 1) {
            return -1.f;   // estop
        } else if (super.speedStepMode == SpeedStepMode128) {
            return ((lSpeed - 1) / 126.f);
        } else {
            return (int) (lSpeed * 27.f + 0.5) + 1;
        }
    }

    public void reply(TamsReply m) {
        if (m.match("WARNING") >= 0) {
            return;
        }
        if (m.match("L " + address.getNumber()) >= 0) {
            try {
                String[] lines = m.toString().split(" ");
                Float newSpeed = new Float(floatSpeed(Integer.parseInt(lines[2])));
                super.setSpeedSetting(newSpeed);
                if (lines[3].equals("1") && !this.f0) {
                    notifyPropertyChangeListener(Throttle.F0, this.f0, true);
                    this.f0 = true;
                } else if (lines[3].equals("0") && this.f0) {
                    notifyPropertyChangeListener(Throttle.F0, this.f0, false);
                    this.f0 = false;
                }
                if (lines[4].equals("r") && isForward) {
                    notifyPropertyChangeListener("IsForward", isForward, false);
                    isForward = false;
                } else if (lines[4].equals("f") && !isForward) {
                    notifyPropertyChangeListener("IsForward", isForward, true);
                    isForward = true;
                }
                if (lines[5].equals("1") && !this.f1) {
                    notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                    this.f1 = true;
                } else if (lines[5].equals("0") && this.f1) {
                    notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                    this.f1 = false;
                }
                if (lines[6].equals("1") && !this.f2) {
                    notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                    this.f2 = true;
                } else if (lines[6].equals("0") && this.f2) {
                    notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                    this.f2 = false;
                }
                if (lines[7].equals("1") && !this.f3) {
                    notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                    this.f3 = true;
                } else if (lines[7].equals("0") && this.f3) {
                    notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                    this.f3 = false;
                }
                if (lines[8].equals("1") && !this.f4) {
                    notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                    this.f4 = true;
                } else if (lines[8].equals("0") && this.f4) {
                    notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                    this.f4 = false;
                }
            } catch (Exception ex) {
                log.error("Error phrasing reply from MC " + ex);
            }
        } else if (m.match("FX " + address.getNumber()) >= 0) {
            String[] lines = m.toString().split(" ");
            try {
                if (lines[2].equals("1") && !this.f9) {
                    notifyPropertyChangeListener(Throttle.F9, this.f9, true);
                    this.f9 = true;
                } else if (lines[2].equals("0") && this.f9) {
                    notifyPropertyChangeListener(Throttle.F9, this.f9, false);
                    this.f9 = false;
                }
                if (lines[3].equals("1") && !this.f10) {
                    notifyPropertyChangeListener(Throttle.F10, this.f10, true);
                    this.f10 = true;
                } else if (lines[3].equals("0") && this.f10) {
                    notifyPropertyChangeListener(Throttle.F10, this.f10, false);
                    this.f10 = false;
                }
                if (lines[4].equals("1") && !this.f11) {
                    notifyPropertyChangeListener(Throttle.F11, this.f11, true);
                    this.f11 = true;
                } else if (lines[4].equals("0") && this.f11) {
                    notifyPropertyChangeListener(Throttle.F11, this.f11, false);
                    this.f11 = false;
                }
                if (lines[5].equals("1") && !this.f12) {
                    notifyPropertyChangeListener(Throttle.F12, this.f12, true);
                    this.f12 = true;
                } else if (lines[5].equals("0") && this.f12) {
                    notifyPropertyChangeListener(Throttle.F12, this.f12, false);
                    this.f12 = false;
                }
                if (lines[6].equals("1") && !this.f13) {
                    notifyPropertyChangeListener(Throttle.F13, this.f13, true);
                    this.f13 = true;
                } else if (lines[6].equals("0") && this.f13) {
                    notifyPropertyChangeListener(Throttle.F13, this.f13, false);
                    this.f13 = false;
                }
                if (lines[7].equals("1") && !this.f14) {
                    notifyPropertyChangeListener(Throttle.F14, this.f14, true);
                    this.f14 = true;
                } else if (lines[7].equals("0") && this.f14) {
                    notifyPropertyChangeListener(Throttle.F14, this.f14, false);
                    this.f14 = false;
                }
            } catch (Exception ex) {
                log.error("Error phrasing reply from MC " + ex);
            }
        } else if (m.match("F " + address.getNumber()) >= 0) {
            String[] lines = m.toString().split(" ");
            try {
                if (lines[2].equals("1") && !this.f1) {
                    notifyPropertyChangeListener(Throttle.F1, this.f1, true);
                    this.f1 = true;
                } else if (lines[2].equals("0") && this.f1) {
                    notifyPropertyChangeListener(Throttle.F1, this.f1, false);
                    this.f1 = false;
                }
                if (lines[3].equals("1") && !this.f2) {
                    notifyPropertyChangeListener(Throttle.F2, this.f2, true);
                    this.f2 = true;
                } else if (lines[3].equals("0") && this.f2) {
                    notifyPropertyChangeListener(Throttle.F2, this.f2, false);
                    this.f2 = false;
                }
                if (lines[4].equals("1") && !this.f3) {
                    notifyPropertyChangeListener(Throttle.F3, this.f3, true);
                    this.f3 = true;
                } else if (lines[4].equals("0") && this.f3) {
                    notifyPropertyChangeListener(Throttle.F3, this.f3, false);
                    this.f3 = false;
                }
                if (lines[5].equals("1") && !this.f4) {
                    notifyPropertyChangeListener(Throttle.F4, this.f4, true);
                    this.f4 = true;
                } else if (lines[5].equals("0") && this.f4) {
                    notifyPropertyChangeListener(Throttle.F4, this.f4, false);
                    this.f4 = false;
                }

                if (lines[6].equals("1") && !this.f5) {
                    notifyPropertyChangeListener(Throttle.F5, this.f5, true);
                    this.f5 = true;
                } else if (lines[6].equals("0") && this.f5) {
                    notifyPropertyChangeListener(Throttle.F5, this.f5, false);
                    this.f5 = false;
                }
                if (lines[7].equals("1") && !this.f6) {
                    notifyPropertyChangeListener(Throttle.F6, this.f6, true);
                    this.f6 = true;
                } else if (lines[7].equals("0") && this.f6) {
                    notifyPropertyChangeListener(Throttle.F6, this.f6, false);
                    this.f6 = false;
                }
                if (lines[8].equals("1") && !this.f7) {
                    notifyPropertyChangeListener(Throttle.F7, this.f7, true);
                    this.f7 = true;
                } else if (lines[8].equals("0") && this.f7) {
                    notifyPropertyChangeListener(Throttle.F7, this.f7, false);
                    this.f7 = false;
                }
                if (lines[9].equals("1") && !this.f8) {
                    notifyPropertyChangeListener(Throttle.F8, this.f8, true);
                    this.f8 = true;
                } else if (lines[9].equals("0") && this.f8) {
                    notifyPropertyChangeListener(Throttle.F8, this.f8, false);
                    this.f8 = false;
                }
            } catch (Exception ex) {
                log.error("Error phrasing reply from MC " + ex);
            }
        } else if (m.toString().equals("ERROR: no data.")) {
            log.debug("Loco has no data");
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(TamsThrottle.class.getName());

}
