package jmri.jmrit.logixng.util.swing;

import jmri.jmrit.logixng.util.parser.InvalidSyntaxException;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test FormulaDiagram
 *
 * @author Daniel Bergqvist 2022
 */
public class FormulaDiagramTest {

    @Test
    public void test1() throws InvalidSyntaxException, ParserException {

        Assert.assertEquals(String.format(
                "12%n"),
                FormulaDiagram.getDiagram("12"));
    }

    @Test
    public void test2() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "            +%n" +
                "           / \\       %n" +
                "      /---/   \\---\\%n" +
                "     /             \\ %n" +
                "    -               *%n" +
                "   / \\             / \\   %n" +
                "  /   \\           /   \\%n" +
                " /     \\         /     \\ %n" +
                "a       b       c       d%n"),
                FormulaDiagram.getDiagram("(a-b) + c*d"));
    }

    @Test
    public void test3() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "          +%n" +
                "         / \\     %n" +
                "      /-/   \\-\\%n" +
                "     /         \\ %n" +
                "    -           !%n" +
                "   / \\          |%n" +
                "  /   \\         |%n" +
                " /     \\        |%n" +
                "a       b       c%n"),
                FormulaDiagram.getDiagram("(a-b)+!c"));
    }

    @Test
    public void test4() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                     ? :%n" +
                "                     /|\\-------\\%n" +
                "             /------/ |         \\%n" +
                "            /          \\--\\      \\ %n" +
                "           +              d       e%n" +
                "          / \\      %n" +
                "      /--/   \\--\\%n" +
                "     /           \\ %n" +
                "    -             !%n" +
                "   / \\            |%n" +
                "  /   \\           |%n" +
                " /      \\         |%n" +
                "a       bbb       c%n"),
                FormulaDiagram.getDiagram("(a-bbb)+!c?d:e"));
    }

    @Test
    public void test5() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                         ? :%n" +
                "                         /|\\              %n" +
                "             /----------/ | \\-----------\\%n" +
                "            /             |              \\ %n" +
                "           +              d               +%n" +
                "          / \\                            / \\      %n" +
                "      /--/   \\--\\                    /--/   \\--\\%n" +
                "     /           \\                  /           \\ %n" +
                "    -             !               eee            *%n" +
                "   / \\            |                             / \\    %n" +
                "  /   \\           |                            /   \\%n" +
                " /      \\         |                          /       \\ %n" +
                "a       bbb       c                         f       ggggg%n"),
                FormulaDiagram.getDiagram("(a-bbb)+!c?d:(eee+f*ggggg)"));
    }

    @Test
    public void test6() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                                               ? :%n" +
                "              /--------------------------------/|\\                                    %n" +
                "             /                                  | \\--------------------------------\\%n" +
                "            /              /-------------------/                                    \\ %n" +
                "           +              d                                                          +%n" +
                "          / \\                                                                       / \\                 %n" +
                "      /--/   \\--\\                                                    /-------------/   \\-------------\\%n" +
                "     /           \\                                                  /                                 \\ %n" +
                "    -             !                                                +                                fffffff%n" +
                "   / \\            |                                               / \\                   %n" +
                "  /   \\           |                              /---------------/   \\---------------\\%n" +
                " /      \\         |                             /                                     \\ %n" +
                "a       bbb       c                            +                                  jjjjjjjjjjj%n" +
                "                                              / \\           %n" +
                "                                     /-------/   \\-------\\%n" +
                "                                    /                     \\ %n" +
                "                                  eee                      *%n" +
                "                                                          / \\         %n" +
                "                                                   /-----/   \\-----\\%n" +
                "                                                  /                 \\ %n" +
                "                                                 *              hhhhhhhhhhh%n" +
                "                                                / \\    %n" +
                "                                               /   \\%n" +
                "                                             /       \\ %n" +
                "                                            f       ggggg%n"),
                FormulaDiagram.getDiagram("(a-bbb)+!c?d:(eee+f*ggggg*hhhhhhhhhhh+jjjjjjjjjjj+fffffff)"));
    }

    @Test
    public void test7() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                                                                      ? :%n" +
                "                                                                      /|\\------------------------------\\%n" +
                "                                      /------------------------------/ |                                \\%n" +
                "                                     /                                  \\-------------------------\\      \\ %n" +
                "                                    +                                                             d       c%n" +
                "                                   / \\                               %n" +
                "      /---------------------------/   \\----------------------------\\%n" +
                "     /                                                              \\ %n" +
                "    -                                                                !%n" +
                "   / \\                                                               |%n" +
                "  /   \\                                                              |%n" +
                " /      \\                                                            |%n" +
                "a       bbb                                                          +%n" +
                "                                                                    / \\                 %n" +
                "                                                     /-------------/   \\-------------\\%n" +
                "                                                    /                                 \\ %n" +
                "                                                   +                                fffffff%n" +
                "                                                  / \\                   %n" +
                "                                 /---------------/   \\---------------\\%n" +
                "                                /                                     \\ %n" +
                "                               +                                  jjjjjjjjjjj%n" +
                "                              / \\           %n" +
                "                     /-------/   \\-------\\%n" +
                "                    /                     \\ %n" +
                "                  eee                      *%n" +
                "                                          / \\         %n" +
                "                                   /-----/   \\-----\\%n" +
                "                                  /                 \\ %n" +
                "                                 *              hhhhhhhhhhh%n" +
                "                                / \\    %n" +
                "                               /   \\%n" +
                "                             /       \\ %n" +
                "                            f       ggggg%n"),
                FormulaDiagram.getDiagram("(a-bbb)+!(eee+f*ggggg*hhhhhhhhhhh+jjjjjjjjjjj+fffffff)?d:c"));
    }

    @Test
    public void test8() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format("12%n"),
                FormulaDiagram.getDiagram("12"));
    }

    @Test
    public void test9() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "     +%n" +
                "    / \\   %n" +
                "   /   \\%n" +
                "  /      \\ %n" +
                "12       31%n"),
                FormulaDiagram.getDiagram("12+31"));
    }

    @Test
    public void test10() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "       +%n" +
                "      / \\    %n" +
                "     /   \\%n" +
                "   /        \\ %n" +
                "12345       31%n"),
                FormulaDiagram.getDiagram("12345+31"));
    }

    @Test
    public void test11() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "           +%n" +
                "          / \\     %n" +
                "       /-/   \\--\\%n" +
                "      /          \\ %n" +
                "     *            2%n" +
                "    / \\   %n" +
                "   /   \\%n" +
                "  /      \\ %n" +
                "12       31%n"),
                FormulaDiagram.getDiagram("12*31+2"));
    }

    @Test
    public void test12() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "       +%n" +
                "      / \\     %n" +
                "   /-/   \\--\\%n" +
                "  /          \\ %n" +
                "12            *%n" +
                "             / \\   %n" +
                "            /   \\%n" +
                "           /      \\ %n" +
                "         31       23%n"),
                FormulaDiagram.getDiagram("12+31*23"));
    }

    @Test
    public void test13() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "         +%n" +
                "        / \\      %n" +
                "    /--/   \\---\\%n" +
                "   /            \\ %n" +
                "12345            *%n" +
                "                / \\   %n" +
                "               /   \\%n" +
                "              /      \\ %n" +
                "            31       23%n"),
                FormulaDiagram.getDiagram("12345+31*23"));
    }

    @Test
    public void test14() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "              *%n" +
                "             / \\        %n" +
                "       /----/   \\----\\%n" +
                "      /               \\ %n" +
                "     +                 -%n" +
                "    / \\               / \\   %n" +
                "   /   \\             /   \\%n" +
                "  /      \\          /     \\ %n" +
                "12       31       23       1%n"),
                FormulaDiagram.getDiagram("(12+31)*(23-1)"));
    }

    @Test
    public void test15() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                *%n" +
                "               / \\        %n" +
                "         /----/   \\-----\\%n" +
                "        /                \\ %n" +
                "       +                  -%n" +
                "      / \\                / \\   %n" +
                "     /   \\              /   \\%n" +
                "   /        \\          /     \\ %n" +
                "12345       31       23       1%n"),
                FormulaDiagram.getDiagram("(12345+31)*(23-1)"));
    }

    @Test
    public void test16() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                   *%n" +
                "                  / \\          %n" +
                "          /------/   \\------\\%n" +
                "         /                   \\ %n" +
                "        +                     -%n" +
                "       / \\                   / \\   %n" +
                "    /-/   \\--\\              /   \\%n" +
                "   /          \\            /     \\ %n" +
                "12345       312345       23       1%n"),
                FormulaDiagram.getDiagram("(12345+312345)*(23-1)"));
    }

    @Test
    public void test17() throws InvalidSyntaxException, ParserException {
        Assert.assertEquals(String.format(
                "                                  +%n" +
                "                                 / \\                         %n" +
                "          /---------------------/   \\---------------------\\%n" +
                "         /                                                 \\ %n" +
                "        +                                                   /%n" +
                "       / \\                                                 / \\       %n" +
                "   /--/   \\---\\                                       /---/   \\---\\%n" +
                "  /            \\                                     /             \\ %n" +
                "12              *                                   -              23%n" +
                "               / \\                                 / \\      %n" +
                "            /-/   \\--\\                         /--/   \\--\\%n" +
                "           /          \\                       /           \\ %n" +
                "         31            -                     +             2%n" +
                "                      / \\                   / \\     %n" +
                "                     /   \\               /-/   \\-\\%n" +
                "                    /     \\             /         \\ %n" +
                "                  23       1           *           3%n" +
                "                                      / \\   %n" +
                "                                     /   \\%n" +
                "                                    /     \\ %n" +
                "                                   9       2%n"),
                FormulaDiagram.getDiagram("12+31*(23-1)+((9*2+3)-2)/23"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
