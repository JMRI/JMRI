package jmri.util.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import jmri.util.JUnitUtil;
import jmri.util.swing.XTableColumnModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * Test simple functioning of JTableToCsvAction
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
public class JTableToCsvActionTest  {

    
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testInitComponents() throws Exception{
        // for now, just makes sure there isn't an exception.
        
        JTable table = new JTable(DATA, COLUMNS);
        
        assertThat(
            new JTableToCsvAction("ActionName",table,table.getModel(),"FileName",new int[]{1}))
            .isNotNull();
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCsvFromModel() throws IOException {
        
        JTable table = new JTable(new TestModel());
        AbstractAction action = new JTableToCsvAction(
    "ActionName",null,table.getModel(),"FileName",new int[]{1});
        
        assertThat( this.tempDir.toFile().isDirectory()).isTrue();
        
        // create a thread that waits to close the dialog box opened later
        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( "Save" );
            new JButtonOperator(jfo,"Save").doClick();
        });
        dialog_thread.setName("Save CSV Model Dialog Close Thread");
        dialog_thread.start();
        
        action.actionPerformed(null);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Save Model Dialog closed");
        
        File file = tempDir.resolve("FileName.csv").toFile();
        assertThat(file).isNotNull();
        
        String[][] mainArray = arrayFromFile(file);
        
        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            int linenum=0;
            // read the first line from the text file
            String line = br.readLine();
            // loop until all lines are read
            while (line != null) {
                mainArray[linenum++] = line.split(",");
                line = br.readLine();
            }
        }

        assertEquals(2,mainArray[0].length);
        assertEquals(2,mainArray[1].length);
        assertEquals(2,mainArray[2].length);
        assertEquals(2,mainArray[3].length);
        
        assertEquals(COLUMNS[0],mainArray[0][0]);
        assertEquals(COLUMNS[2],mainArray[0][1]);

        assertEquals(DATA[0][0],mainArray[1][0]);
        assertEquals(DATA[0][2],mainArray[1][1]);
        
        assertEquals(DATA[1][0],mainArray[2][0]);
        assertEquals(DATA[1][2],mainArray[2][1]);
        
        String tmp = (mainArray[3][0]).replace("\"", "");
        assertThat(tmp).isEmpty();
        assertEquals(DATA[2][2],mainArray[3][1]);
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testCsvFromTable() throws java.io.IOException {
        
        AbstractTableModel model = new TestModel();
        JTable table = new JTable(model);
        AbstractAction action = new JTableToCsvAction(
    "ActionName",table,table.getModel(),"FileName2.csv",new int[]{});
    
        XTableColumnModel tcm = new XTableColumnModel();
        table.setColumnModel(tcm);
        table.createDefaultColumnsFromModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(2), false);
        table.moveColumn(0, 1);
        
        table.setAutoCreateRowSorter(true);
        
        TableRowSorter<AbstractTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("A", 0));
        
        assertEquals(2,table.getColumnCount(),"2 cols");
        assertEquals(1,table.getRowCount(),"1 row");
        
        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( "Save" );
            new JButtonOperator(jfo,"Save").doClick();
        });
        dialog_thread.setName("Save CSV Dialog Table Close Thread");
        dialog_thread.start();
        
        action.actionPerformed(null);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Save Dialog Table closed");
        
        File file = tempDir.resolve("FileName2.csv").toFile();
        
        String[][] mainArray = arrayFromFile(file);
        
        assertEquals(2,mainArray[0].length);
        assertEquals(2,mainArray[1].length);
        assertThat(mainArray[2]).isNull();
        
        
        assertEquals("Column2",mainArray[0][0]);
        assertEquals("Column1",mainArray[0][1]);
        
        assertEquals("2A",mainArray[1][0]);
        assertEquals("1A",mainArray[1][1]);
        
    }
    
    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testExistingFile() throws java.io.IOException {
        
        AbstractTableModel model = new TestModel();
        JTable table = new JTable(model);
        AbstractAction action = new JTableToCsvAction(
    "ActionName",table,table.getModel(),"Name.csv",new int[]{});
    
        Thread dialog_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( "Save" );
            new JButtonOperator(jfo,"Save").doClick();
        });
        dialog_thread.setName("Save CSV Dialog Table Close Thread");
        
        Thread dialog_thread2 = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( "Save" );
            new JButtonOperator(jfo,"Save").doClick();
        });
        dialog_thread2.setName("Save CSV Dialog Table Close Thread");
        
        dialog_thread.start();
        
        
        Thread question_thread = new Thread(() -> {
            JDialogOperator jfo = new JDialogOperator( Bundle.getMessage("ConfirmQuestion") );
            new JButtonOperator(jfo,"Yes").doClick();
        });
        question_thread.setName("Overwrite existing Close Thread");
        question_thread.start();
        
        
        
        action.actionPerformed(null);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread.isAlive());}, "Save Dialog Table closed");
        
        String[][] mainArray = arrayFromFile(tempDir.resolve("Name.csv").toFile());
        
        assertThat(mainArray[0][0]).isNotEmpty();

        dialog_thread2.start();
        
        action.actionPerformed(null);
        
        JUnitUtil.waitFor(()->{return !(dialog_thread2.isAlive());}, "Save Dialog Table closed");
        JUnitUtil.waitFor(()->{return !(question_thread.isAlive());}, "Overwrite Dialog closed");
        
        String[][] mainArray2 = arrayFromFile(tempDir.resolve("Name.csv").toFile());
        
        assertThat(mainArray2[0][0]).isNotEmpty();
        
        assertThat(java.util.Arrays.equals(mainArray[0], mainArray2[0])).isTrue();
        assertThat(java.util.Arrays.equals(mainArray[1], mainArray2[1])).isTrue();

    }
    
    private String[][] arrayFromFile(File file) throws IOException {
        
        assertThat(file).isNotNull();
    
        String[][] tmpArray = new String[4][];
        
        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            int linenum=0;
            // read the first line from the text file
            String line = br.readLine();
            // loop until all lines are read
            while (line != null) {
                tmpArray[linenum++] = line.split(",");
                line = br.readLine();
            }
        }
        //catch (IOException e) {
        //    assertThat(tmpArray[0][0]=="").isTrue();
        //}
        return tmpArray;
    }
    
    private final String[] COLUMNS = {"Column1", "Column2", "Column3"};

    private final Object[][] DATA = {
        {"1A", "2A","3A"},
        {"1B", "2B","3B"},
        {null, "2C","3C"} };
    
    private class TestModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getRowCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMNS[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return DATA[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {}

    }
        
    @TempDir 
    protected Path tempDir;

    @BeforeEach
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));
        
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
