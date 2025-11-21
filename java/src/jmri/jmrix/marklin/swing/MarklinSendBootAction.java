package jmri.jmrix.marklin.swing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to send MCAN BOOT message to Märklin devices.
 * <p>
 * This action sends a CAN BOOT command (0xB1) to invoke the bootloader
 * update sequence for Märklin hardware. According to German language 
 * forum documentation, this is part of the software/bootloader command 
 * range used for firmware updates and device initialization.
 *
 * @author JMRI Community
 * @see <a href="https://www.stummiforum.de/t122854f7-M-rklin-CAN-Protokoll-x-B-commands-updates.html">Märklin CAN Protokoll 0x1B commands documentation</a>
 */
public class MarklinSendBootAction extends AbstractAction {

    private final MarklinSystemConnectionMemo memo;
    private static final Logger log = LoggerFactory.getLogger(MarklinSendBootAction.class);

    /**
     * Create an action to send MCAN BOOT message.
     *
     * @param name the name for this action; will appear on menu items, buttons, etc.
     * @param memo the system connection memo for this action
     */
    public MarklinSendBootAction(String name, MarklinSystemConnectionMemo memo) {
        super(name);
        this.memo = memo;
    }

    /**
     * Create an action with default name.
     *
     * @param memo the system connection memo for this action
     */
    public MarklinSendBootAction(MarklinSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSendMCanBoot"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (memo != null && memo.getTrafficController() != null) {
            MarklinMessage bootMessage = MarklinMessage.getCanBoot();
            memo.getTrafficController().sendMarklinMessage(bootMessage, null);
            log.info("CanBoot Message sent");
        } else {
            log.warn("Cannot send CanBoot message - no connection available");
        }
    }
}
