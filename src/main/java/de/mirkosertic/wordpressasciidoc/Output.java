package de.mirkosertic.wordpressasciidoc;

public interface Output {
    void print(String aString);

    void print(char aCharacter);

    void println();
}
