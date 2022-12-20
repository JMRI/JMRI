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

        System.out.format("%n------------------------%n");
        for (int i=0; i < output.size(); i++) {
            System.out.println(output.get(i).toString());
        }
        System.out.format("%n%n");
    }

    private void printDiagram(ExprNodeData exprNodeData, List<StringBuilder> output) {
        if (output.size() <= exprNodeData._y) {
            output.add(new StringBuilder());
        }

        StringBuilder sb = output.get(exprNodeData._y);
        int pad = exprNodeData._x - sb.length();
//        System.out.format("Token: %s, x: %d, sb.len: %d%n", exprNodeData._str, exprNodeData._x, sb.length(), pad);
        if (pad > 0) {
            // Pad string with spaces
            sb.append(String.format(String.format("%%%ds", pad), ""));
        }
        sb.append(exprNodeData._str);

        for (int i=0; i < exprNodeData._childs.size(); i++) {
            printDiagram(exprNodeData._childs.get(i), output);
        }
    }

    private void calculateDiagramPass1(ExpressionNode exprNode, ExprNodeData exprNodeData, int y) {
        TokenType tokenType = exprNode.getToken().getTokenType();
        exprNodeData._str = tokenType.hasData() ? exprNode.getToken().getString() : tokenType.getString();
        exprNodeData._y = y;

        for (int i=0; i < exprNode.getChildCount(); i++) {
            exprNodeData._childs.add(new ExprNodeData());
            calculateDiagramPass1(exprNode.getChild(i), exprNodeData._childs.get(i), y+1);
        }
    }

    private void moveRight(ExprNodeData exprNodeData, int size) {
        if (size == 0) return;

        exprNodeData._x += size;

        for (int i=0; i < exprNodeData._childs.size(); i++) {
            moveRight(exprNodeData, size);
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


        int myStrDiv2 = exprNodeData._str.length() / 2;
        int myCenter = x0 + myStrDiv2;

        if (!exprNodeData._childs.isEmpty()) {
            width -= space;

            int centerLeft = exprNodeData._childs.get(0).getCenter();
            int centerRight = exprNodeData._childs.get(exprNodeData._childs.size()-1).getCenter();
            int childCenter = (centerLeft + centerRight) / 2;

//            System.out.format("Token: %s, myCenter: %d, centerLeft: %d, centerRight: %d, childCenter: %d%n", exprNodeData._str, myCenter, centerLeft, centerRight, childCenter);

            if (childCenter < myCenter) {
                double addPerChild = (myCenter - childCenter) / (exprNodeData._childs.size() - 1);
                moveRight(exprNodeData, (int) Math.round(addPerChild));
//                System.out.format("Token: %s, addPerChild: %1.1f%n", exprNodeData._str, addPerChild);
            } else {
                myCenter = childCenter;
//                System.out.format("Token: %s, myCenter: %d%n", exprNodeData._str, myCenter);
            }
        }


//        int left = (width - exprNodeData._str.length()) / 2;
        exprNodeData._x = myCenter - myStrDiv2;
//        if (left < 0) left = 0;
//        exprNodeData._x = x0 + left;

        return Math.max(exprNodeData._str.length(), width);
    }


    private static class ExprNodeData {
        private String _str;
        private int _x, _y;
        List<ExprNodeData> _childs = new ArrayList<>();

        public int getCenter() {
            return _x + _str.length()/2;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FormulaDiagram.class);
}
