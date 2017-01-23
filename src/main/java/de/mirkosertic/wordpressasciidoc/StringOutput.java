package de.mirkosertic.wordpressasciidoc;

public class StringOutput implements Output {

    private final StringBuilder result;

    public StringOutput() {
        result = new StringBuilder();
    }

    @Override
    public void print(String aString) {
        result.append(aString);
    }

    @Override
    public void print(char aCharacter) {
        result.append(aCharacter);
    }

    @Override
    public void println() {
        result.append("\n");
    }

    @Override
    public String toString() {
        return result.toString();
    }
}