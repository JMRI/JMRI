package jmri.jmrit.logixng.util.swing;

import java.util.*;

import jmri.jmrit.logixng.util.parser.*;

/**
 * Shows a diagram of a formula.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class FormulaDiagram {

    public void showDiagram(String formula) {
        try {
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            ExpressionNode exprNode = parser.parseExpression(formula);
            showDiagram(exprNode);
        } catch (ParserException ex) {
            log.error("Invalid formula '{}'. Error: ", formula, ex);
        }
    }

    public void showDiagram(ExpressionNode exprNode) {
        if (exprNode == null) return;

        ExprNodeData exprNodeData = new ExprNodeData();

        calculateDiagramPass1(exprNode, exprNodeData, 0);
        calculateDiagramPass2(exprNodeData, 0);

        List<StringBuilder> output = new ArrayList<>();
        printDiagram(exprNodeData, output);

//        System.out.format("%n------------------------%nDiagram:%n");
        System.out.format("%n------------------------%n");
        for (int i=0; i < output.size(); i++) {
            System.out.println(output.get(i).toString());
        }
        System.out.format("%n%n");
//        System.out.format("------------------------%n");
    }

    private void printDiagram(ExprNodeData exprNodeData, List<StringBuilder> output) {
//        System.out.format("printDiagram: %s, x: %d, y: %d%n", exprNodeData._str, exprNodeData._x, exprNodeData._y);

        if (output.size() <= exprNodeData._y) {
            output.add(new StringBuilder());
        }

        StringBuilder sb = output.get(exprNodeData._y);
//        if (sb.length() < exprNodeData._x) {
            // Pad string with spaces
//            sb.append(String.format(String.format("%%%ds", exprNodeData._x - sb.length()), ""));
//        }
//        System.out.println("Format: '"+String.format("%%%ds", exprNodeData._x)+"'");
        int pad = exprNodeData._x - sb.length();
//        int pad = exprNodeData._x;
        if (pad > 0) {
            sb.append(String.format(String.format("%%%ds", pad), ""));
        }
        sb.append(exprNodeData._str);

        for (int i=0; i < exprNodeData._childs.size(); i++) {
            printDiagram(exprNodeData._childs.get(i), output);
        }
    }

    private void calculateDiagramPass1(ExpressionNode exprNode, ExprNodeData exprNodeData, int y) {
//        System.out.format("calculateDiagramPass1: %s, %d%n", exprNode.getDefinitionString(), y);

        TokenType tokenType = exprNode.getToken().getTokenType();
        exprNodeData._str = tokenType.hasData() ? exprNode.getToken().getString() : tokenType.getString();
        exprNodeData._y = y;

        for (int i=0; i < exprNode.getChildCount(); i++) {
            exprNodeData._childs.add(new ExprNodeData());
            calculateDiagramPass1(exprNode.getChild(i), exprNodeData._childs.get(i), y+1);
        }
    }

    private int calculateDiagramPass2(ExprNodeData exprNodeData, int x0) {
        final int space = 7;
        int width = 0;
        for (int i=0; i < exprNodeData._childs.size(); i++) {
            ExprNodeData data = exprNodeData._childs.get(i);
            width += Math.max(data._str.length(), calculateDiagramPass2(exprNodeData._childs.get(i), x0+width));
            width += space;
        }
        if (!exprNodeData._childs.isEmpty()) width -= space;

//        System.out.format("calculateDiagramPass2: %s, %d, %d%n", exprNodeData._str, exprNodeData._y, width);

        int left = (width - exprNodeData._str.length()) / 2;
        if (left < 0) left = 0;
        exprNodeData._x = x0 + left;

        return Math.max(exprNodeData._str.length(), width);
    }


    private static class ExprNodeData {
//        private ExpressionNode _exprNode;
        private String _str;
        private int _x, _y;
        List<ExprNodeData> _childs = new ArrayList<>();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FormulaDiagram.class);
}
