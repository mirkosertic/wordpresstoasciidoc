package de.mirkosertic.wordpressasciidoc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WordPressToAsciiDoctorTest {

    @Test
    public void testH1() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<h1>This is a Test</h1>");
        assertEquals("= This is a Test\n", theOutput.toString());
    }

    @Test
    public void testH2() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<h2>This is a Test</h2>");
        assertEquals("== This is a Test\n", theOutput.toString());
    }

    @Test
    public void testH3() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<h3>This is a Test</h3>");
        assertEquals("=== This is a Test\n", theOutput.toString());
    }

    @Test
    public void testH4() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<h4>This is a Test</h4>");
        assertEquals("==== This is a Test\n", theOutput.toString());
    }

    @Test
    public void testH5() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<h5>This is a Test</h5>");
        assertEquals("===== This is a Test\n", theOutput.toString());
    }

    @Test
    public void testLI() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<ul><li>This is a Test</li></ul>");
        assertEquals("\n * This is a Test\n", theOutput.toString());
    }

    @Test
    public void testBold() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<bold>Test</bold>");
        assertEquals("*Test*", theOutput.toString());
    }

    @Test
    public void testLink() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<a href=\"http://unknown\" target=\"_blank\">Test</a>");
        assertEquals("http://unknown[Test] ", theOutput.toString());
    }

    @Test
    public void testImg() {
        StringOutput theOutput = new StringOutput();
        WordPressToAsciiDoctor theWordPressToAsciiDoctor = new WordPressToAsciiDoctor(theOutput);
        theWordPressToAsciiDoctor.parse("<img src=\"http://unknown\" width=\"100\" height=\"120\"/>Test");
        assertEquals("image:http://unknown[100,120]Test", theOutput.toString());
    }
}