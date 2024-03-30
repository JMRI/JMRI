package jmri.jmrix.mqtt;

import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.regex.*;


/**
 * An implementation of AbstractThrottle with code specific to a MQTT
 * connection.
 *
 * @author Dean Cording (C) 2023
 */


 public class MqttThrottle extends AbstractThrottle implements MqttEventListener{

    private final MqttAdapter mqttAdapter;
    @Nonnull
    public String sendThrottleTopic = "cab/{0}/throttle";
    @Nonnull
    public String rcvThrottleTopic ="cab/{0}/throttle";
    @Nonnull
    public String sendDirectionTopic = "cab/{0}/direction";
    @Nonnull
    public String rcvDirectionTopic = "cab/{0}/direction";
    @Nonnull
    public String sendFunctionTopic = "cab/{0}/function/{1}";
    @Nonnull
    public String rcvFunctionTopic = "cab/{0}/function/{1}";

    protected int address = -1;

    private Pattern functionPattern;

    private MqttConsistManager consistManager;

   /**
     * Constructor.
     * @param memo system connection.
     */

    public MqttThrottle(MqttSystemConnectionMemo memo) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();

        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward
        log.debug("MqttThrottle constructor");
    }



    public MqttThrottle(MqttSystemConnectionMemo memo, String sendThrottleTopic, String rcvThrottleTopic,
                    String sendDirectionTopic, String rcvDirectionTopic, String sendFunctionTopic,
                    String rcvFunctionTopic) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();
        this.sendThrottleTopic = sendThrottleTopic;
        this.rcvThrottleTopic = rcvThrottleTopic;
        this.sendDirectionTopic = sendDirectionTopic;
        this.rcvDirectionTopic = rcvDirectionTopic;
        this.sendFunctionTopic = sendFunctionTopic;
        this.rcvFunctionTopic = rcvFunctionTopic;

        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward
        log.debug("MqttThrottle constructor");
    }

    /**
     * Constructor.
     * @param memo system connection
     * @param sendThrottleTopic     MQTT topic for sending speed
     * @param rcvThrottleTopic      MQTT topic for receiving speed
     * @param sendDirectionTopic    MQTT topic for sending direction
     * @param rcvDirectionTopic     MQTT topic for receiving direction
     * @param sendFunctionTopic     MQTT topic for sending function values
     * @param rcvFunctionTopic      MQTT topic for receiving function values
     * @param address loco address to set on throttle
     */
    public MqttThrottle(MqttSystemConnectionMemo memo, String sendThrottleTopic, String rcvThrottleTopic,
        String sendDirectionTopic, String rcvDirectionTopic, String sendFunctionTopic, String rcvFunctionTopic, LocoAddress address) {
        super(memo);
        mqttAdapter = memo.getMqttAdapter();
        consistManager = memo.getConsistManager();
        this.sendThrottleTopic = sendThrottleTopic;
        this.rcvThrottleTopic = rcvThrottleTopic;
        this.sendDirectionTopic = sendDirectionTopic;
        this.rcvDirectionTopic = rcvDirectionTopic;
        this.sendFunctionTopic = sendFunctionTopic;
        this.rcvFunctionTopic = rcvFunctionTopic;

        this.setDccAddress(address.getNumber());
        this.speedStepMode = SpeedStepMode.NMRA_DCC_128;

        this.isForward = true; //loco should default to forward

        log.debug("MqttThrottle constructor called for address {}", address);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpeedSetting(float speed) {

        super.setSpeedSetting(speed);

        if (speed < 0) {
            speed = 0;
            // Send MQTT message
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), "STOP");
            });
            super.setSpeedSetting(0);
            log.debug("sent address {} direction {}", address, "STOP");
        }

        int intSpeed = Math.round(speed * 100);
        
        // ensure non-zero input will result in non-zero output
        if (speed > 0 && intSpeed == 0)
        {
            intSpeed = 1;
        }

        final String stringSpeed = String.valueOf(intSpeed);
        
        // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayout(() -> {

            mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), stringSpeed);
        });
        log.debug("sent address {} speed {}", address, intSpeed);


    }

    /**
     * Set the direction
     *
     * @param forward true if forward; false otherwise
     */
    @Override
    public void setIsForward(boolean forward) {

        super.setIsForward(forward);
         // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), (forward ? "FORWARD" : "REVERSE"));
        });
        log.debug("sent address {} direction {}", address, (forward ? "FORWARD" : "REVERSE"));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendFunctionGroup(int functionNum, boolean momentary) {

        // Send MQTT message
        jmri.util.ThreadingUtil.runOnLayoutEventually(() -> {
            mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\{0\\}", String.valueOf(address)).replaceFirst("\\{1\\}",String.valueOf(functionNum)), (getFunction(functionNum) ? "ON" : "OFF"));
        });

        log.debug("sent address {} function {} {}", address, functionNum, (getFunction(functionNum) ? "ON" : "OFF"));

    }


    protected void throttleRelease() {

        active = false;

        // Send blank MQTT message to remove any persistent message
        jmri.util.ThreadingUtil.runOnLayout(() -> {
            mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), "");
            mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), "");

            for (int functionNum = 0; functionNum < getFunctions().length; functionNum++) {
                mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\{0\\}",
                    String.valueOf(address)).replaceFirst("\\{1\\}",String.valueOf(functionNum)), "");
            }
        });
        consistManager.deactivateConsist(getLocoAddress());

        mqttAdapter.unsubscribe(this.rcvThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
        mqttAdapter.unsubscribe(this.rcvDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
        mqttAdapter.unsubscribe(this.rcvFunctionTopic.replaceFirst("\\{0\\}", String.valueOf(address)).replaceFirst("\\{1\\}", "+"), this);


    }

    /**
     * Dispose when finished with this object. After this, further usage of this
     * Throttle object will result in a JmriException.
     *
     * This is quite problematic, because a using object doesn't know when it's
     * the last user.
     */
    @Override
    protected void throttleDispose() {
        log.debug("throttleDispose {}", address);

        finishRecord();
    }



    public int setDccAddress(int newaddress) {

        if (address > 0) {
            // Send blank MQTT message to remove any persistent message
            jmri.util.ThreadingUtil.runOnLayout(() -> {
                mqttAdapter.publish(this.sendThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), "");
                mqttAdapter.publish(this.sendDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), "");

                for (int functionNum = 0; functionNum < getFunctions().length; functionNum++) {
                    mqttAdapter.publish(this.sendFunctionTopic.replaceFirst("\\{0\\}",
                        String.valueOf(address)).replaceFirst("\\{1\\}",String.valueOf(functionNum)), "");
                }
            });

            mqttAdapter.unsubscribe(this.rcvThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
            mqttAdapter.unsubscribe(this.rcvDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
            mqttAdapter.unsubscribe(this.rcvFunctionTopic.replaceFirst("\\{0\\}", String.valueOf(address)).replaceFirst("\\{1\\}", "+"), this);
        }
        address = newaddress;

        mqttAdapter.subscribe(this.rcvThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
        mqttAdapter.subscribe(this.rcvDirectionTopic.replaceFirst("\\{0\\}", String.valueOf(address)), this);
        mqttAdapter.subscribe(this.rcvFunctionTopic.replaceFirst("\\{0\\}", String.valueOf(address)).replaceFirst("\\{1\\}", "+"), this);

        consistManager.activateConsist(getLocoAddress());
        setSpeedSetting(0);
        setIsForward(true);

        functionPattern = Pattern.compile(this.rcvFunctionTopic.replaceFirst("\\{0\\}",
            String.valueOf(address)).replaceFirst("\\{1\\}", "(\\\\d+)"));

        return address;
    }

    public int getDccAddress() {
        return address;
    }

    @Override
    public LocoAddress getLocoAddress() {
        return new DccLocoAddress(address, MqttThrottleManager.isLongAddress(address));
    }

    @Override
    public void notifyMqttMessage(String receivedTopic, String message) {

        if (receivedTopic.endsWith(this.rcvThrottleTopic.replaceFirst("\\{0\\}", String.valueOf(address)))) {

            Float speed ;

            try {
                speed = Math.max(0.0f,Math.min(Float.parseFloat(message)/100.0f,1.0f));
            }
            catch (Exception e){
                if (message.length() != 0) {
                    log.error("Invalid throttle speed: '{}'", message);
                }
                speed = -1.0f;
            }

            super.setSpeedSetting(speed);

        } else if (receivedTopic.endsWith(this.rcvDirectionTopic.replaceFirst("\\{0\\}",
                    String.valueOf(address)))) {
            switch (message) {
                case "FORWARD":
                    super.setIsForward(true);
                    break;
                case "REVERSE":
                    super.setIsForward(false);
                    break;
                case "STOP":
                case "":
                    super.setSpeedSetting(-1);
                    break;
                default:
                    log.error("Invalid message {}", message);
            }
        } else {

            Matcher functionMatcher = functionPattern.matcher(receivedTopic);
            if (functionMatcher.matches()) {
                updateFunction(Integer.parseInt(functionMatcher.group(1)),(message.equals("ON")));
            }
        }
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(MqttThrottle.class);

 }
