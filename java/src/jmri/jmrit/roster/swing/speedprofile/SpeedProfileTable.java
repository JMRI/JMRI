package jmri.jmrit.roster.swing.speedprofile;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import jmri.implementation.SignalSpeedMap;
import jmri.jmrit.roster.RosterSpeedProfile;
import jmri.jmrit.roster.RosterSpeedProfile.SpeedStep;
/**
 * Display Speed Profile
 *
 * @author  Pete Cressman Copyright (C) 2015
 * @version $Revision: 1.5 $
 */
public class SpeedProfileTable extends jmri.util.JmriJFrame {

    private static final long serialVersionUID = 765383251598012193L;
    java.text.DecimalFormat threeDigit = new java.text.DecimalFormat("0.000");
    int interp;
    float scale;

    public SpeedProfileTable(RosterSpeedProfile sp, String rosterId) {
        super(false, true);
        setTitle(Bundle.getMessage("SpeedTable"));
        getContentPane().setLayout(new BorderLayout(15,15));
        
        interp = SignalSpeedMap.getMap().getInterpretation();
        scale = SignalSpeedMap.getMap().getLayoutScale();
        SpeedTableModel model = new SpeedTableModel(sp);
        JTable table = new JTable(model);
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
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
        
        String str;
        switch(interp) {
            case SignalSpeedMap.SPEED_MPH:
            case SignalSpeedMap.SPEED_KMPH:
                str = "scale";
                break;
            default:
                str = "track";
        }
        JLabel prompt = new JLabel(Bundle.getMessage("rosterId", Bundle.getMessage(str), rosterId));
        contentPane.add(prompt, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(table);
        contentPane.add(pane, BorderLayout.CENTER);

        getContentPane().add(contentPane);
        pack();
    }

    
    class SpeedTableModel extends javax.swing.table.AbstractTableModel {
        static final int STEP_COL = 0;
        static final int THROTTLE_COL = 1;
        static final int FORWARD_SPEED_COL = 2;
        static final int FORWARD_FACTOR_COL = 3;
        static final int REVERSE_SPEED_COL = 4;
        static final int REVERSE_FACTOR_COL = 5;
        static final int NUMCOLS = 6;
        private static final long serialVersionUID = 865383251598012193L;
        
        ArrayList<Map.Entry<Integer, SpeedStep>> speedArray = new  ArrayList<Map.Entry<Integer, SpeedStep>>();
        
        SpeedTableModel(RosterSpeedProfile sp) {
            TreeMap<Integer, SpeedStep> speeds = sp.getProfileSpeeds();
            Map.Entry<Integer, SpeedStep> entry = speeds.firstEntry();
            while (entry!=null) {
                speedArray.add(entry);
                entry = speeds.higherEntry(entry.getKey());
            }
        }
        public int getColumnCount() {
            return NUMCOLS;
        }

        @Override
        public int getRowCount() {
            return speedArray.size();
        }
        public String getColumnName(int col) {
            String rate;
            switch(interp) {
                case SignalSpeedMap.SPEED_MPH:
                    rate = "Mph";
                    break;
                case SignalSpeedMap.SPEED_KMPH:
                    rate = "KMph";
                    break;
                default:
                    rate = "mm/s";
            }
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
            }
            return "";
        }
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
            }
            return new JTextField(8).getPreferredSize().width;
        }
        
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
            }
            return "";
        }

        public void setValueAt(Object value, int row, int col) {
        }
    }
}
