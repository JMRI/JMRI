package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
/**
 * Display Speed Profile
 *
 * @author  Pete Cressman Copyright (C) 2015
 */
public class SpeedProfileTable extends jmri.util.JmriJFrame {

    java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
    int interp;
    float scale;
    JLabel description;
    String rosterId;
    RosterEntry re;

    public SpeedProfileTable(RosterEntry rosterEntry) {
        super(false, true);
        re = rosterEntry;
        rosterId = re.getId();
        setTitle(Bundle.getMessage("SpeedTable", rosterId));
        getContentPane().setLayout(new BorderLayout(15,15));
        
        interp = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getInterpretation();
        scale = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getLayoutScale();
        SpeedTableModel model = new SpeedTableModel(re.getSpeedProfile());
        JTable table = new JTable(model);
        table.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent ke) {
                char ch = ke.getKeyChar(); 
                if (ch == KeyEvent.VK_DELETE || ch == KeyEvent.VK_X) {
                    deleteRow(table);
                }
            }
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {}
        });
        
        for (int i = 0; i < model.getColumnCount(); i++) {
            int width = model.getPreferredWidth(i);
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(width);
            if (i>0) {
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                renderer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                column.setCellRenderer(renderer);                
            }
        }
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        java.awt.Font font = table.getFont();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel(Bundle.getMessage("units"));
        label.setFont(font);
        javax.swing.ButtonGroup bp = new javax.swing.ButtonGroup();
        JRadioButton mm = new JRadioButton(Bundle.getMessage("mm"));
        mm.setFont(font);
        mm.addActionListener((ActionEvent e) -> {
            update(model, SignalSpeedMap.PERCENT_NORMAL);
        });
        JRadioButton mph = new JRadioButton(Bundle.getMessage("mph"));
        mph.setFont(font);
        mph.addActionListener((ActionEvent e) -> {
            update(model, SignalSpeedMap.SPEED_MPH);
        });
        JRadioButton kph = new JRadioButton(Bundle.getMessage("kph"));
        kph.setFont(font);
        kph.addActionListener((ActionEvent e) -> {
            update(model, SignalSpeedMap.SPEED_KMPH);
        });
        bp.add(mm);
        bp.add(mph);
        bp.add(kph);
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        panel.add(mm);
        panel.add(mph);
        panel.add(kph);
        panel.add(Box.createHorizontalGlue());
        String str;
        switch(interp) {
            case SignalSpeedMap.SPEED_MPH:
                mph.setSelected(true);
                str = "scale";
                break;
            case SignalSpeedMap.SPEED_KMPH:
                kph.setSelected(true);
                str = "scale";
                break;
            default:
                mm.setSelected(true);
                str = "track";
        }
        description = new JLabel(Bundle.getMessage("rosterId", Bundle.getMessage(str), rosterId));
        description.setFont(font);
        description.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        
        contentPane.add(description, BorderLayout.NORTH);
        contentPane.add(panel, BorderLayout.CENTER);
        JScrollPane pane = new JScrollPane(table);
        contentPane.add(pane, BorderLayout.SOUTH);

        getContentPane().add(contentPane);
        pack();
    }

    private void deleteRow(JTable table) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            SpeedTableModel model = (SpeedTableModel)table.getModel();
            Map.Entry<Integer, SpeedStep> entry = model.speedArray.get(row);
            int step = Math.round((float)(entry.getKey()*126)/1000);
            if ( JOptionPane.YES_OPTION  == JOptionPane.showConfirmDialog(null, 
                    Bundle.getMessage("DeleteRow", step), Bundle.getMessage("SpeedTable", rosterId),
                    JOptionPane.YES_NO_OPTION)) {
                model.speedArray.remove(entry);
                re.getSpeedProfile().deleteStep(entry.getKey());
                model.fireTableDataChanged();
                re.updateFile();
                Roster.getDefault().writeRoster();
            }
        }
    }
    
    private void update(SpeedTableModel model, int which) {
        interp = which;
        // can't figure out a way to update column names
        //model.getColumnName(SpeedTableModel.FORWARD_SPEED_COL);
        //model.getColumnName(SpeedTableModel.REVERSE_SPEED_COL);
        String str;
        switch(interp) {
            case SignalSpeedMap.SPEED_MPH:
            case SignalSpeedMap.SPEED_KMPH:
                str = "scale";
                break;
            default:
                str = "track";
        }
        description.setText(Bundle.getMessage("rosterId", Bundle.getMessage(str), rosterId));
        model.fireTableDataChanged();
    }

    
    class SpeedTableModel extends javax.swing.table.AbstractTableModel {
        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int FORWARD_SPEED_COL = 2;
        static final int FORWARD_FACTOR_COL = 3;
        static final int REVERSE_SPEED_COL = 4;
        static final int REVERSE_FACTOR_COL = 5;
        static final int NUMCOLS = 6;
        
        ArrayList<Map.Entry<Integer, SpeedStep>> speedArray = new  ArrayList<Map.Entry<Integer, SpeedStep>>();
        
        SpeedTableModel(RosterSpeedProfile sp) {
            TreeMap<Integer, SpeedStep> speeds = sp.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.firstEntry();
            while (entry!=null) {
                speedArray.add(entry);
                entry = speeds.higherEntry(entry.getKey());
            }
        }
        
        @Override
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return speedArray.size();
        }
        @Override
        public String getColumnName(int col) {
            String rate = Bundle.getMessage("speed");
            /* can't figure out a way to update column names
            switch(interp) {
                case SignalSpeedMap.SPEED_MPH:
                    rate = "Mph";
                    break;
                case SignalSpeedMap.SPEED_KMPH:
                    rate = "KMph";
                    break;
                default:
                    rate = "mm/s";
            }*/
            switch (col) {
                case STEP_COL:
                    return Bundle.getMessage("step");
                case THROTTLE_COL:
                    return Bundle.getMessage("throttle");
                case FORWARD_SPEED_COL:
                    return Bundle.getMessage("forwardSpeed", rate);
                case REVERSE_SPEED_COL:
                    return Bundle.getMessage("reverseSpeed", rate);
                case FORWARD_FACTOR_COL:
                case REVERSE_FACTOR_COL:
                    return Bundle.getMessage("factor");
                default:
                    // fall out
                    break;
            }
            return "";
        }
        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case STEP_COL:
                    return new JTextField(3).getPreferredSize().width;
                case THROTTLE_COL:
                case FORWARD_FACTOR_COL:
                case REVERSE_FACTOR_COL:
                    return new JTextField(5).getPreferredSize().width;
                case FORWARD_SPEED_COL:
                case REVERSE_SPEED_COL:
                    return new JTextField(8).getPreferredSize().width;
                default:
                    // fall out
                    break;
            }
            return new JTextField(8).getPreferredSize().width;
        }
        
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<Integer, SpeedStep> entry = speedArray.get(row);
            switch (col) {
                case STEP_COL:
                    return Math.round((float)(entry.getKey()*126)/1000);
                case THROTTLE_COL:
                    return (float)(entry.getKey())/1000;
                case FORWARD_SPEED_COL:
                    float speed = entry.getValue().getForwardSpeed();
                    switch(interp) {
                        case SignalSpeedMap.SPEED_MPH:
                            speed = speed*scale*3.6f*0.621371f/1000;
                            break;
                        case SignalSpeedMap.SPEED_KMPH:
                            speed = speed*scale*3.6f/1000;
                            break;
                        default:
                    }
                    return threeDigit.format(speed);
                case FORWARD_FACTOR_COL:
                    return threeDigit.format(entry.getValue().getForwardSpeed()/entry.getKey());
                case REVERSE_SPEED_COL:
                    speed = entry.getValue().getReverseSpeed();
                    switch(interp) {
                        case SignalSpeedMap.SPEED_MPH:
                            speed = speed*scale*3.6f*0.621371f/1000;
                            break;
                        case SignalSpeedMap.SPEED_KMPH:
                            speed = speed*scale*3.6f/1000;
                            break;
                        default:
                    }
                    return threeDigit.format(speed);
                case REVERSE_FACTOR_COL:
                    return threeDigit.format(entry.getValue().getReverseSpeed()/entry.getKey());
                default:
                    // fall out
                    break;
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
        }
    }
}
