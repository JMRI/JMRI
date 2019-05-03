package jmri.jmrix.sprog.sprogslotmon;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.sprog.sprogslotmon.SprogSlotMonDataModel;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a command station slot manager.
 * <p>
 * May-17 Modified to a SprogListener to handle status replies.
 * <p>
 * Jan-18 Moved status request generation here, based on a timer.
 *
 * @author	Bob Jacobsen Copyright (C) 2001 
 * @author  Andrew Crosland (C) 2006 ported to SPROG 2008
 */
public class SprogSlotMonFrame extends jmri.util.JmriJFrame implements SprogListener {

    /**
     * Controls whether not-in-use slots are shown.
     */
    javax.swing.JCheckBox showAllCheckBox = new javax.swing.JCheckBox();

    JButton estopAllButton = new JButton(Bundle.getMessage("ButtonEstopAll"));
    SprogSlotMonDataModel slotModel = null;

    JTable slotTable;
    JScrollPane slotScroll;

    JTextArea status = new JTextArea(Bundle.getMessage("TrackCurrentXString", "---"));

    SprogSystemConnectionMemo _memo = null;
    private SprogTrafficController tc = null;
    
    private static final int STATUS_PERIOD = 500;
    javax.swing.Timer timer = null;

    public SprogSlotMonFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
        
        tc = memo.getSprogTrafficController();
        tc.addSprogListener(this);
        
        slotModel = new SprogSlotMonDataModel(SprogSlotMonDataModel.getSlotCount(), 8,_memo);

        slotTable = new JTable(slotModel);
        slotTable.setRowSorter(new TableRowSorter<>(slotModel));
        slotScroll = new JScrollPane(slotTable);

        // configure items for GUI
        showAllCheckBox.setText(Bundle.getMessage("ButtonShowUnusedSlots"));
        showAllCheckBox.setVisible(true);
        showAllCheckBox.setSelected(true);
        showAllCheckBox.setToolTipText(Bundle.getMessage("ButtonShowSlotsTooltip"));

        slotModel.configureTable(slotTable);

        // add listener object so checkboxes function
        showAllCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                slotModel.showAllSlots(showAllCheckBox.isSelected());
                slotModel.fireTableDataChanged();
            }
        });

        // add listener object so stop all button functions
        estopAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.debug("Estop all button pressed");
                _memo.getCommandStation().estopAll();
            }
        });

        estopAllButton.addMouseListener(new MouseListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                _memo.getCommandStation().estopAll();
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });

        // adjust model to default settings
        slotModel.showAllSlots(showAllCheckBox.isSelected());

        // general GUI config
        setTitle(Bundle.getMessage("SprogSlotMonitorTitle"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel pane1 = new JPanel();
        pane1.setLayout(new FlowLayout());

        pane1.add(showAllCheckBox);
        pane1.add(estopAllButton);
        pane1.add(status);

        getContentPane().add(pane1);
        getContentPane().add(slotScroll);

        setHelp();
        
        pack();
        pane1.setMaximumSize(pane1.getSize());
        pack();
        
        startTimer(STATUS_PERIOD);
    }

    /**
     * Define system-specific help item
     */
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.sprog.sprogslotmon.SprogSlotMonFrame", true);  // NOI18N
    }

    /**
     * Find the existing SprogSlotMonFrame object.
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public final SprogSlotMonFrame instance() {
        return null;
    }

    public void update() {
        slotModel.fireTableDataChanged();
    }

    public void updateStatus(String a) {
        status.setText(Bundle.getMessage("TrackCurrentXString", a));
    }

    private boolean mShown = false;

    /**
     * Listen to outgoing messages.
     *
     * @param m the sprog message received
     */
    @Override
    public void notifyMessage(SprogMessage m) {
        // Do nothing
    }

    /**
     * Listen for status replies.
     * 
     * @param m The SprogReply to be handled
     */
    @Override
    public void notifyReply(SprogReply m) {
        int [] statusA = new int[4];
        String s = m.toString();
        log.debug("Reply received: {}", s);
        if (s.indexOf('S') > -1) {
            // Handle a status reply
            log.debug("Status reply");
            int i = s.indexOf('h');
            // Double Check that "h" was found in the reply
            if (i > -1) {
                int milliAmps = (int) ((Integer.decode("0x" + s.substring(i + 7, i + 11))) * 
                            tc.getAdapterMemo().getSprogType().getCurrentMultiplier());
                statusA[0] = milliAmps;
                String ampString;
                ampString = Float.toString((float) statusA[0] / 1000);
                updateStatus(ampString);
            }
        }
    }
    
    @Override
    public void addNotify() {
        super.addNotify();

        if (mShown) {
            return;
        }

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    @Override
    public void dispose() {
        // deregister with the command station.
        stopTimer();
	if(slotModel!=null) {
           slotModel.dispose();
	}
        slotModel = null;
        slotTable = null;
        slotScroll = null;
	if(tc!=null) {
           tc.removeSprogListener(this);
	}
        super.dispose();
    }

    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        Runnable r = () -> {
            // Send a status request
            log.debug("Sending status request");
            tc.sendSprogMessage(SprogMessage.getStatus(), this);
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts
     * 
     * @param delay timer delay
     */
    protected void startTimer(int delay) {
        log.debug("Restart timer");
        if (timer == null) {
            timer = new javax.swing.Timer(delay, (java.awt.event.ActionEvent e) -> {
                timeout();
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Internal routine to handle timer stop
     */
    protected void stopTimer() {
        log.debug("Stop timer");
        if (timer != null) {
            timer.stop();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SprogSlotMonFrame.class);

}
