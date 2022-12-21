package jmri.jmrit.logixng.util.swing;

import java.util.*;

import jmri.jmrit.logixng.util.parser.*;

/**
 * Shows a diagram of a formula.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class FormulaDiagram {

    private FormulaDiagram() {
        // This class should never be instantiated.
    }

    public static String getDiagram(String formula) throws ParserException {
        Map<String, Variable> variables = new HashMap<>();
        RecursiveDescentParser parser = new RecursiveDescentParser(variables);
        ExpressionNode exprNode = parser.parseExpression(formula);
        return getDiagram(exprNode);
    }

    public static String getDiagram(ExpressionNode exprNode) {
        if (exprNode == null) return "";

        ExprNodeData exprNodeData = new ExprNodeData();

        calculateDiagramPass1(exprNode, exprNodeData, 0);
        calculateDiagramPass2(exprNodeData, 0);

        List<StringBuilder> output = new ArrayList<>();
        printDiagram(exprNodeData, output);

        String newLine = String.format("%n");
        StringBuilder sb = new StringBuilder();
        // The last three lines are empty
        for (int i=0; i < output.size()-3; i++) {
            sb.append(output.get(i).toString()).append(newLine);
        }
        return sb.toString();
    }

    private static String pad(char ch, int size) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < size; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private static void drawLines(List<StringBuilder> output, int y, int numLines, int center, int x1, int x2, int width) {

        StringBuilder sb1 = output.get(y * 4 + 1);
        StringBuilder sb2 = output.get(y * 4 + 2);
        StringBuilder sb3 = output.get(y * 4 + 3);
        sb1.append(pad(' ', x1 - sb1.length()));
        sb2.append(pad(' ', x1 - sb2.length()));
        sb3.append(pad(' ', x1 - sb3.length()));

        int sb1Len = sb1.length();
        int sb2Len = sb2.length();
        int sb3Len = sb3.length();

        // Calculate how to draw lines
        switch (numLines) {
            case 0:
                // Do nothing
                break;

            case 1:
                sb1.append(pad(' ', center - sb1.length())).append("|");
                sb2.append(pad(' ', center - sb2.length())).append("|");
                sb3.append(pad(' ', center - sb3.length())).append("|");
                break;

            case 2:
//                System.out.format("aa: %d%n", center - sb2Len - 5);
                sb1.append(pad(' ', center - sb1Len - 1))
                        .append("/ \\")
                        .append(pad(' ', center - sb1Len - 1));
                switch (center - sb2.length() - 5) {
                    case -2:
                        sb2.append(",,,");
                        break;
                    case -1:
                        sb2.append("  ")
                                .append("/   \\");
                        break;
                    case 0:
                        sb2.append("   ")
                                .append("/   \\");
                        break;
                    default:
                        sb2.append("  /")
                                .append(pad('-', center - sb2Len - 5))
                                .append("/   \\")
                                .append(pad('-', x2 - center - 5))
                                .append("\\");
                }
                sb3.append(" /")
                        .append(pad(' ', x2 - sb3Len - 3))
                        .append("\\ ");
                break;

            case 3:
                break;

            default:
                throw new RuntimeException("Too many children. Only 0 - 3 children is possible. Num children: "+Integer.toString(numLines));
        }

//        sb1.append(padRight);
//        sb2.append(padRight);
//        sb3.append(padRight);
    }




    private static void drawLines3(List<StringBuilder> output, int y, int center, int x1, int x2, int x3, int width) {

        StringBuilder sb1 = output.get(y * 4 + 1);
        StringBuilder sb2 = output.get(y * 4 + 2);
        StringBuilder sb3 = output.get(y * 4 + 3);
        sb1.append(pad(' ', x1 - sb1.length()));
        sb2.append(pad(' ', x1 - sb2.length()));
        sb3.append(pad(' ', x1 - sb3.length()));

        int sb1Len = sb1.length();
        int sb2Len = sb2.length();
        int sb3Len = sb3.length();



        if (center > x2) {
            sb1.append("   /")
                    .append(pad('-', center - sb1Len - 5))
                    .append("/|\\")
                    .append(pad(' ', center - sb1Len - 1));
            switch (center - sb2.length() - 5) {
                case -1:
                    sb2.append("  ")
                            .append("/ | \\");
                    break;
                case 0:
                    sb2.append("   ")
                            .append("/ | \\");
                    break;
                default:
                    sb2.append("  /")
                            .append(pad(' ', center - sb2Len - 3))
                            .append("| \\")
                            .append(pad('-', x3 - center - 5))
                            .append("\\");
            }
            sb3.append(" /")
                    .append(pad(' ', x2 - sb3Len - 1))
                    .append('/')
                    .append(pad('-', center - x2 - 3))
                    .append("/  ")
                    .append(pad(' ', x3 - center - 3))
                    .append("\\ ");

        } else if (center == x2) {
            sb1.append(pad(' ', center - sb1Len - 1))
                    .append("/|\\")
                    .append(pad(' ', center - sb1Len - 1));
            switch (center - sb2.length() - 5) {
                case -1:
                    sb2.append("  ")
                            .append("/ | \\");
                    break;
                case 0:
                    sb2.append("   ")
                            .append("/ | \\");
                    break;
                default:
                    sb2.append("  /")
                            .append(pad('-', center - sb2Len - 5))
                            .append("/ | \\")
                            .append(pad('-', x3 - center - 5))
                            .append("\\");
            }
            sb3.append(" /")
                    .append(pad(' ', center - sb3Len - 2))
                    .append("|")
                    .append(pad(' ', x3 - center - 2))
                    .append("\\ ");

        } else {    // center < x2
            sb1.append(pad(' ', center - sb1Len - 1))
                    .append("/|\\")
                    .append(pad('-', x3 - center - 5))
                    .append("\\");
            switch (center - sb2.length() - 5) {
                case -1:
                    sb2.append("  ")
                            .append("/ | \\");
                    break;
                case 0:
                    sb2.append("   ")
                            .append("/ | \\");
                    break;
                default:
                    sb2.append("  /")
                            .append(pad('-', center - sb2Len - 5))
                            .append("/ |")
                            .append(pad(' ', x3 - center - 3))
                            .append("\\");
            }
            sb3.append(" /")
                    .append(pad(' ', center - sb3Len - 1))
                    .append('\\')
                    .append(pad('-', x2 - center - 2))
                    .append("\\")
                    .append(pad(' ', x3 - x2 - 2))
                    .append("\\ ");
        }
    }




    private static void printDiagram(ExprNodeData exprNodeData, List<StringBuilder> output) {
        if (output.size() <= exprNodeData._y*4) {
            for (int i=0; i < 4; i++) {
                output.add(new StringBuilder());
            }
        }

        StringBuilder sb = output.get(exprNodeData._y * 4);
        int pad = exprNodeData._x0 - sb.length();
//        System.out.format("Token: %s, x: %d, sb.len: %d%n", exprNodeData._str, exprNodeData._x, sb.length(), pad);
        if (pad > 0) {
            // Pad string with spaces
            sb.append(String.format(String.format("%%%ds", pad), ""));
        }
        sb.append(exprNodeData._str);

        int numChilds = exprNodeData._childs.size();
        int lineX1 = numChilds > 0 ? exprNodeData._childs.get(0)._center : 0;
        int lineX2 = numChilds > 1 ? exprNodeData._childs.get(1)._center : 0;
        int lineX3 = numChilds > 2 ? exprNodeData._childs.get(2)._center : 0;

        if (numChilds == 3) {
            // Operator ? :
            drawLines3(output, exprNodeData._y, exprNodeData._center, lineX1, lineX2, lineX3, exprNodeData._x1);
        } else {
            drawLines(output, exprNodeData._y, numChilds, exprNodeData._center, lineX1, lineX2, exprNodeData._x1);
        }

        for (int i=0; i < numChilds; i++) {
            printDiagram(exprNodeData._childs.get(i), output);
        }
    }

    private static void calculateDiagramPass1(ExpressionNode exprNode, ExprNodeData exprNodeData, int y) {
        TokenType tokenType = exprNode.getToken().getTokenType();
        exprNodeData._str = tokenType.hasData() ? exprNode.getToken().getString() : tokenType.getString();
        exprNodeData._y = y;

        for (int i=0; i < exprNode.getChildCount(); i++) {
            exprNodeData._childs.add(new ExprNodeData());
            calculateDiagramPass1(exprNode.getChild(i), exprNodeData._childs.get(i), y+1);
        }
    }

    private static void moveRight(ExprNodeData exprNodeData, int size) {
        if (size == 0) return;

        exprNodeData._x0 += size;

        for (int i=0; i < exprNodeData._childs.size(); i++) {
            moveRight(exprNodeData, size);
        }
    }

    private static int calculateDiagramPass2(ExprNodeData exprNodeData, int x0) {
        final int space = 7;
        int width = 0;
        for (int i=0; i < exprNodeData._childs.size(); i++) {
            ExprNodeData data = exprNodeData._childs.get(i);
            width += Math.max(data._str.length(), calculateDiagramPass2(exprNodeData._childs.get(i), x0+width));
            width += space;
        }


        int myStrDiv2 = exprNodeData._str.length() / 2;
        exprNodeData._center = x0 + myStrDiv2;

        if (!exprNodeData._childs.isEmpty()) {
            width -= space;

            int centerLeft = exprNodeData._childs.get(0).getCenter();
            int centerRight = exprNodeData._childs.get(exprNodeData._childs.size()-1).getCenter();
            int childCenter = (centerLeft + centerRight) / 2;

//            System.out.format("Token: %s, myCenter: %d, centerLeft: %d, centerRight: %d, childCenter: %d%n", exprNodeData._str, myCenter, centerLeft, centerRight, childCenter);

            if (childCenter < exprNodeData._center) {
                double addPerChild = ((double)exprNodeData._center - childCenter) / (exprNodeData._childs.size() - 1);
                moveRight(exprNodeData, (int) Math.round(addPerChild));
//                System.out.format("Token: %s, addPerChild: %1.1f%n", exprNodeData._str, addPerChild);
            } else {
                exprNodeData._center = childCenter;
//                System.out.format("Token: %s, myCenter: %d%n", exprNodeData._str, myCenter);
            }
        }


//        int left = (width - exprNodeData._str.length()) / 2;
        exprNodeData._x0 = exprNodeData._center - myStrDiv2;
//        if (left < 0) left = 0;
//        exprNodeData._x = x0 + left;

        exprNodeData._x1 = exprNodeData._x0 + exprNodeData._str.length();
        if (!exprNodeData._childs.isEmpty()
                && exprNodeData._childs.get(exprNodeData._childs.size()-1)._x1 > exprNodeData._x1) {
            exprNodeData._x1 = exprNodeData._childs.get(exprNodeData._childs.size()-1)._x1;
        }

        return Math.max(exprNodeData._str.length(), width);
    }


    private static class ExprNodeData {
        private String _str;
        private int _x0, _center, _y, _x1;
        List<ExprNodeData> _childs = new ArrayList<>();

        public int getCenter() {
            return _x0 + _str.length()/2;
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FormulaDiagram.class);
}
