package jmri.util.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.util.CsvUtil;

import org.apache.commons.csv.CSVFormat;

/**
 * Panel that allows the selection of org.apache.commons.csv.CSVFormat.
 * 
 * @author Daniel Bergqvist (C) 2022
 */
public class CsvUtilPanel extends JPanel {

    public CsvUtilPanel(CSVFormat csvFormat, CSVFormat.Predefined predefinedCsvFormat) {

        JPanel p = this;
        p.setBorder(BorderFactory.createLineBorder(java.awt.Color.black));
//        p.setLayout(new FlowLayout());
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();


        JLabel _csvTypeLabel = new JLabel("Table type");
        JLabel _delimiterLabel = new JLabel("Delimiter");
        JLabel _escapeLabel = new JLabel("Escape");
        JLabel _quoteLabel = new JLabel("Quote");
        JLabel _recordSeparatorLabel = new JLabel("Record separator");
        JCheckBox _ignoreSurrondingSpaces = new JCheckBox("Ignore surronding spaces");
        JComboBox<CsvUtil.CSVPredefinedFormat> _csvType = new JComboBox<>();
        for (CsvUtil.CSVPredefinedFormat format : CsvUtil.CSVPredefinedFormat.getFormats()) {
            _csvType.addItem(format);
        }
        JComboBox<CsvUtil.CSVDelimiter> _delimiter = new JComboBox<>();
        for (CsvUtil.CSVDelimiter delimiter : CsvUtil.CSVDelimiter.getDelimiters()) {
            _delimiter.addItem(delimiter);
        }
        JComboBox<CsvUtil.CSVEscape> _escape = new JComboBox<>();
        for (CsvUtil.CSVEscape escape : CsvUtil.CSVEscape.getEscapes()) {
            _escape.addItem(escape);
        }
        JComboBox<CsvUtil.CSVQuote> _quote = new JComboBox<>();
        for (CsvUtil.CSVQuote escape : CsvUtil.CSVQuote.getQuotes()) {
            _quote.addItem(escape);
        }
        JComboBox<CsvUtil.CSVRecordSeparator> _recordSeparator = new JComboBox<>();
        for (CsvUtil.CSVRecordSeparator rs : CsvUtil.CSVRecordSeparator.getRecordSeparators()) {
            _recordSeparator.addItem(rs);
        }

        _csvType.addActionListener((evt) -> {
            CsvUtil.CSVPredefinedFormat format = _csvType.getItemAt(_csvType.getSelectedIndex());
            if (format.getFormat() != null) {
                CSVFormat f = format.getFormat().getFormat();
                _delimiter.setSelectedItem(CsvUtil.CSVDelimiter.parse(f.getDelimiter()));
                _escape.setSelectedItem(CsvUtil.CSVEscape.parse(f.getEscapeCharacter()));
                _quote.setSelectedItem(CsvUtil.CSVQuote.parse(f.getQuoteCharacter()));
                _recordSeparator.setSelectedItem(CsvUtil.CSVRecordSeparator.parse(f.getRecordSeparator()));
                _ignoreSurrondingSpaces.setSelected(f.getIgnoreSurroundingSpaces());
            }
        });

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        p.add(_csvTypeLabel, c);
        _csvTypeLabel.setLabelFor(_csvType);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(_csvType, c);

        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        p.add(_delimiterLabel, c);
        _delimiterLabel.setLabelFor(_delimiterLabel);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(_delimiter, c);

        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        p.add(_recordSeparatorLabel, c);
        _recordSeparatorLabel.setLabelFor(_recordSeparator);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(_recordSeparator, c);

        c.gridx = 0;
        c.gridy = 3;
        c.anchor = GridBagConstraints.EAST;
        p.add(_escapeLabel, c);
        _escapeLabel.setLabelFor(_escape);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(_escape, c);

        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.EAST;
        p.add(_quoteLabel, c);
        _quoteLabel.setLabelFor(_quote);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(_quote, c);

        c.gridx = 1;
        c.gridy = 5;
        c.anchor = GridBagConstraints.WEST;
        p.add(_ignoreSurrondingSpaces, c);
    }

}
