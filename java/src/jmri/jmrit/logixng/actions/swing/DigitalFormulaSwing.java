package jmri.jmrit.logixng.actions.swing;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.CaretEvent;

import jmri.InstanceManager;
import jmri.Reference;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.*;

/**
 * Configures an Formula object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalFormulaSwing extends AbstractDigitalActionSwing {

    private JTextField _formula;
    private JLabel _formulaParentheses;
    private JScrollPane _formulaParenthesesScrollPane;
    private JLabel _formulaError;
    private final Map<String, Variable> variables = new HashMap<>();
    private final RecursiveDescentParser parser = new RecursiveDescentParser(variables);

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {

        DigitalFormula action = (DigitalFormula)object;
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JLabel label = new JLabel(Bundle.getMessage("DigitalFormula_Formula"));
        _formula = new JTextField();
        _formula.setColumns(40);
        _formulaParentheses = new JLabel("AAA");
//        JPanel p = new JPanel();
//        p.add(_formulaParentheses);
        _formulaParenthesesScrollPane = new JScrollPane(_formulaParentheses);
//        _formulaParenthesesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _formulaParenthesesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        _formulaParenthesesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        _formulaParenthesesScrollPane.setPreferredSize(
                new Dimension(_formula.getPreferredSize().width, _formulaParenthesesScrollPane.getPreferredSize().height));
        _formulaError = new JLabel("");
        if (action != null) _formula.setText(action.getFormula());
        _formula.addCaretListener(this::caretListener);
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(label, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        panel.add(_formula, c);
        c.gridy = 1;
        panel.add(_formulaParenthesesScrollPane, c);
        c.gridy = 2;
        panel.add(_formulaError, c);
    }

    private boolean isPosWithingExprNode(
            ExpressionNode exprNode,
            int pos,
            Reference<Integer> start,
            Reference<Integer> end) {

        if (pos < exprNode.getStartPos() || pos >= exprNode.getEndPos()) {
            return false;
        }
        for (int i=0; i < exprNode.getChildCount(); i++) {
            if (isPosWithingExprNode(exprNode.getChild(i), pos, start, end)) {
                return true;
            }
        }
        start.set(exprNode.getStartPos());
        end.set(exprNode.getEndPos());
        return true;
    }

    private void pack() {
        Container c = panel;
        while (c != null && !(c instanceof JDialog)) {
            c = c.getParent();
        }
        if (c != null) {
            ((JDialog)c).pack();
        } else {
            throw new RuntimeException("Panel is not within a JDialog");
        }
    }

    private void caretListener(CaretEvent event) {
        int left = event.getDot();
        int right = event.getMark();
        if (left > right) {
            int swap = left;
            left = right;
            right = swap;
        }

        _formulaError.setText("");

        String color = "#ff9966";
        String text = _formula.getText();

        if (left == right) {
            try {
                ExpressionNode expressionNode = parser.parseExpression(_formula.getText());
                Reference<Integer> startRef = new Reference<>();
                Reference<Integer> endRef = new Reference<>();
                if (isPosWithingExprNode(expressionNode, event.getDot(), startRef, endRef)) {
                    if (startRef.get() == null || endRef.get() == null) {
                        // This cannot happen but Spotbugs warns about it.
                        throw new NullPointerException();
                    }
                    left = startRef.get();
                    right = endRef.get();
                }

            } catch (InvalidSyntaxException ex) {
                log.debug("Error when parsing formula", ex);
                String padAtEnd = "    ";
                left = ex.getPosition();
                right = _formula.getText().length() + padAtEnd.length();
                color = "#ff0000";
                text = _formula.getText() + padAtEnd;
                _formulaError.setText(ex.getLocalizedMessage() + padAtEnd);
            } catch (ParserException | RuntimeException ex) {
                log.error("Error when parsing formula", ex);
                if (ex.getLocalizedMessage() != null) {
                    _formulaError.setText(ex.getLocalizedMessage());
                } else {
                    _formulaError.setText("Null pointer exception");
                }
                _formulaParentheses.setText(_formula.getText());
                pack();
                return;
            }
        }

        String leftText = text.substring(0, left);
        String middleText = text.substring(left, right);
        String rightText = text.substring(right);
        _formulaParentheses.setText("<html>"+leftText+"<font color=\"black\" bgcolor=\""+color+"\">"+middleText+"</font>"+rightText);
//        _formulaParenthesesScrollPane.getHorizontalScrollBar().setValue(_formula.getScrollOffset());

        pack();
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (_formula.getText().isEmpty()) return true;

        try {
            parser.parseExpression(_formula.getText());
        } catch (ParserException ex) {
            errorMessages.add(Bundle.getMessage("DigitalFormula_InvalidFormula", _formula.getText()));
            log.error("Invalid formula '{}'. Error: ", _formula.getText(), ex);
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        DigitalFormula action = new DigitalFormula(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof DigitalFormula)) {
            throw new IllegalArgumentException("object must be an DigitalFormula but is a: "+object.getClass().getName());
        }

        DigitalFormula action = (DigitalFormula)object;

        try {
            action.setFormula(_formula.getText());
        } catch (ParserException ex) {
            log.error("Error when parsing formula", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalFormula_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalFormulaSwing.class);

}
