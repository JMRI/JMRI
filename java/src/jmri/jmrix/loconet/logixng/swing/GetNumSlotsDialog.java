package jmri.jmrix.loconet.logixng.swing;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

import javax.swing.BoxLayout;
import javax.swing.*;

import jmri.*;
import jmri.jmrix.loconet.*;
import jmri.util.ThreadingUtil;

/**
 * This dialog tests how many slots the command station has for engines.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class GetNumSlotsDialog extends JDialog implements ThrottleListener, SlotListener {
    
    private static final int NUM_LOCO_TO_REQUEST = 119;
    
    private final LocoNetSystemConnectionMemo _memo;
    private JTextField _numEnginesField;
    private JTextField _requestLocoField;
    private JLabel _status;
    private int _maxNumLocos = 0;
    private boolean _freeSlots = false;
    private final JTextField _textField;
    private volatile boolean _abort = false;
    
    
    public GetNumSlotsDialog(LocoNetSystemConnectionMemo memo, JTextField textField) {
        super((Frame)null, true);
        _memo = memo;
        _textField = textField;
    }
    
    public void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                _abort = true;
                releaseThrottles();
            }
        });
        
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        JPanel numEnginesPanel = new JPanel();
        numEnginesPanel.add(new JLabel("Current number of engines: "));
        _numEnginesField = new JTextField("0");
        _numEnginesField.setColumns(5);
        _numEnginesField.setEnabled(false);
        numEnginesPanel.add(_numEnginesField);
        
        JPanel requestLocoPanel = new JPanel();
        _requestLocoField = new JTextField("0");
        _requestLocoField.setColumns(5);
        _requestLocoField.setEnabled(false);
        JButton cancelRequestLocoField = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelRequestLocoField.addActionListener((ActionEvent e) -> {
//            new GetNumSlotsDialog().initComponents();
        });
        requestLocoPanel.add(new JLabel("Request loco: "));
        requestLocoPanel.add(_requestLocoField);
        requestLocoPanel.add(cancelRequestLocoField);
        
        getContentPane().add(numEnginesPanel);
        getContentPane().add(requestLocoPanel);
        
        _status = new JLabel("aaa");
        getContentPane().add(_status);
        
        JButton buttonOK = new JButton(Bundle.getMessage("ButtonOK"));
        buttonOK.addActionListener((ActionEvent e) -> {
            _abort = true;
            releaseThrottles();
            dispose();
        });
        getContentPane().add(buttonOK);
        
        pack();
        setLocationRelativeTo(null);
        
        _memo.getSlotManager().addSlotListener(this);
        
        ThreadingUtil.runOnGUIEventually(() -> {
            _freeSlots = true;
            _memo.getSlotManager().update();
        });
        
        // Wait 10 seconds before starting to try request throttles
        ThreadingUtil.runOnGUIDelayed(() -> {
            _freeSlots = false;
            requestThrottle(1);
        },10000);
        
        setVisible(true);
    }
    
    private void requestThrottle(int address) {
//        System.out.format("Request throttle: %d%n", address);
        ThreadingUtil.runOnGUI(() -> {
            _requestLocoField.setText(Integer.toString(address));
        });
//        boolean result = InstanceManager.getDefault(ThrottleManager.class)
        boolean result = _memo.getThrottleManager().requestThrottle(address, this);
        if (!result && (address < NUM_LOCO_TO_REQUEST)) requestThrottle(address+1);
    }
    
    private void updateNumLocos() {
//        System.out.format("Update num locos%n");
        // _maxNumLocos
        int numLocos = 0;
        SlotManager slotManager = _memo.getSlotManager();
        for (int i=1; i <= 119; i++) {
            LocoNetSlot slot = slotManager.slot(i);
            if ((slot.slotStatus() & LnConstants.LOCOSTAT_MASK) != LnConstants.LOCO_FREE) {
                numLocos++;
            }
        }
        
        if (numLocos > _maxNumLocos) {
            _maxNumLocos = numLocos;
            ThreadingUtil.runOnGUI(() -> {
                _numEnginesField.setText(Integer.toString(_maxNumLocos));
                _textField.setText(Integer.toString(_maxNumLocos));
            });
        }
    }
    
    private void releaseThrottles() {
        for (int i=1; i < 120; i++) {
            LocoNetSlot slot = _memo.getSlotManager().slot(i);
            if ((slot.slotStatus() & LnConstants.LOCOSTAT_MASK) != LnConstants.LOCO_FREE) {
                _memo.getLnTrafficController().sendLocoNetMessage(slot.writeStatus(LnConstants.LOCO_FREE));
//                _memo.getLnTrafficController().sendLocoNetMessage(slot.releaseSlot());
            }
        }
    }
    
    @Override
    public void notifyThrottleFound(DccThrottle t) {
//        System.out.format("Throttle found: %d%n", t.getLocoAddress().getNumber());
        updateNumLocos();
        ThreadingUtil.runOnGUI(() -> {
            _status.setText("Throttle was added");
        });
        if (!_abort && (t.getLocoAddress().getNumber() < NUM_LOCO_TO_REQUEST)) {
            ThreadingUtil.runOnGUIDelayed(() -> {
                requestThrottle(t.getLocoAddress().getNumber()+1);
            }, 200);
//            ThreadingUtil.runOnGUIEventually(() -> {
//                requestThrottle(t.getLocoAddress().getNumber()+1);
//            });
        }
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
//        System.out.format("Throttle failed%n");
        ThreadingUtil.runOnGUI(() -> {
            _status.setText(reason);
        });
        if (!_abort && (address.getNumber() < NUM_LOCO_TO_REQUEST)) {
            ThreadingUtil.runOnGUIDelayed(() -> {
                requestThrottle(address.getNumber()+1);
            }, 200);
//            ThreadingUtil.runOnGUIEventually(() -> {
//                requestThrottle(address.getNumber()+1);
//            });
        }
    }

    @Override
    public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
//        System.out.format("Decision required%n");
//        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
//        System.out.format("notifyChangedSlot: %d%n", s.getSlot());
        
        // Try to free slot
        if (_freeSlots
                && (s.getSlot() != 0)
                && (s.slotStatus() != LnConstants.LOCO_FREE)
                && (s.slotStatus() != LnConstants.LOCO_COMMON)) {
//            System.out.format("Send message: %s%n", s.releaseSlot().toMonitorString());
            _memo.getLnTrafficController().sendLocoNetMessage(s.releaseSlot());
        }
    }
    
}
