package de.mirkosertic.wordpressasciidoc;

import java.io.PrintWriter;

public class PrintwriterOutput implements Output {

    private final PrintWriter printWriter;

    public PrintwriterOutput(PrintWriter aPrintWriter) {
        printWriter = aPrintWriter;
    }

    @Override
    public void print(String aString) {
        printWriter.print(aString);
    }

    @Override
    public void print(char aCharacter) {
        printWriter.print(aCharacter);
    }

    @Override
    public void println() {
        printWriter.println();
    }
}
