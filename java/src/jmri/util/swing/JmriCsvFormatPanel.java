package jmri.util.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.util.JmriCsvFormat;

import org.apache.commons.csv.CSVFormat;

/**
 * Panel to configure jmri.util.JmriCsvFormat.
 * 
 * @author Daniel Bergqvist (C) 2022
 */
public class JmriCsvFormatPanel extends JPanel {

    public JmriCsvFormatPanel(JmriCsvFormat csvFormat) {

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
        JComboBox<JmriCsvFormat.CSVPredefinedFormat> _csvType = new JComboBox<>();
        for (JmriCsvFormat.CSVPredefinedFormat format : JmriCsvFormat.CSVPredefinedFormat.getFormats()) {
            _csvType.addItem(format);
        }
        JComboBox<JmriCsvFormat.CSVDelimiter> _delimiter = new JComboBox<>();
        for (JmriCsvFormat.CSVDelimiter delimiter : JmriCsvFormat.CSVDelimiter.getDelimiters()) {
            _delimiter.addItem(delimiter);
        }
        JComboBox<JmriCsvFormat.CSVEscape> _escape = new JComboBox<>();
        for (JmriCsvFormat.CSVEscape escape : JmriCsvFormat.CSVEscape.getEscapes()) {
            _escape.addItem(escape);
        }
        JComboBox<JmriCsvFormat.CSVQuote> _quote = new JComboBox<>();
        for (JmriCsvFormat.CSVQuote escape : JmriCsvFormat.CSVQuote.getQuotes()) {
            _quote.addItem(escape);
        }
        JComboBox<JmriCsvFormat.CSVRecordSeparator> _recordSeparator = new JComboBox<>();
        for (JmriCsvFormat.CSVRecordSeparator rs : JmriCsvFormat.CSVRecordSeparator.getRecordSeparators()) {
            _recordSeparator.addItem(rs);
        }

        _csvType.addActionListener((evt) -> {
            JmriCsvFormat.CSVPredefinedFormat format = _csvType.getItemAt(_csvType.getSelectedIndex());
            if (format.getFormat() != null) {
                CSVFormat f = format.getFormat();
                _delimiter.setSelectedItem(JmriCsvFormat.CSVDelimiter.parse(f.getDelimiterString()));
                _escape.setSelectedItem(JmriCsvFormat.CSVEscape.parse(f.getEscapeCharacter()));
                _quote.setSelectedItem(JmriCsvFormat.CSVQuote.parse(f.getQuoteCharacter()));
                _recordSeparator.setSelectedItem(JmriCsvFormat.CSVRecordSeparator.parse(f.getRecordSeparator()));
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
