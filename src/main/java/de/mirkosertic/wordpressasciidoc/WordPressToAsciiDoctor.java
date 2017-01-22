package de.mirkosertic.wordpressasciidoc;

import java.util.Stack;

public class WordPressToAsciiDoctor {

    public interface Token {
        void process(char theCurrentChar);

        void popped();
    }

    public class DefaultToken implements Token {

        private boolean removeTrailingWhiteSpaces;

        public DefaultToken() {
            removeTrailingWhiteSpaces = true;
        }

        @Override
        public void process(char theCurrentChar) {
            switch (theCurrentChar) {
                case '<':
                    tokenHandler.push(new HTMLToken());
                    break;
                default:
                    if (removeTrailingWhiteSpaces) {
                        switch (theCurrentChar) {
                            case ' ':
                                break;
                            case '\n':
                                break;
                            case '\r':
                                break;
                            default:
                                removeTrailingWhiteSpaces = false;
                                doFirstRun();
                                printer.print(theCurrentChar);
                                break;
                        }
                    } else {
                        printer.print(theCurrentChar);
                        break;
                    }
            }
        }

        protected void doFirstRun() {
        }

        @Override
        public void popped() {
        }
    }

    public class HeaderToken extends DefaultToken {

        private final int level;

        public HeaderToken(int aLevel) {
            level = aLevel;
        }

        @Override
        public void popped() {
            printer.println();
        }

        @Override
        protected void doFirstRun() {
            for (int i=0;i<level;i++) {
                printer.print("=");
            }
            printer.print(" ");
        }
    }

    public class ListItemToken extends DefaultToken {

        @Override
        protected void doFirstRun() {
            printer.print(" * ");
        }

        @Override
        public void popped() {
            printer.println();
        }
    }

    public class BoldToken extends DefaultToken {

        @Override
        protected void doFirstRun() {
            printer.print("*");
        }

        @Override
        public void popped() {
            printer.print("*");
        }
    }

    public class HTMLToken implements Token {

        private String tagName;
        private boolean tagNameComplete;
        private boolean closingTag;

        public HTMLToken() {
            tagName = "";
        }

        private boolean isClosingTagForMyself(Token aHandler) {
            if (aHandler instanceof HTMLToken) {
                HTMLToken theHTML = (HTMLToken) aHandler;
                return tagName.equals(theHTML.tagName);
            }
            return false;
        }

        @Override
        public void process(char theCurrentChar) {
            switch (theCurrentChar) {
                case '>':
                    tagNameComplete = true;
                    if (!closingTag) {
                        switch (tagName.toLowerCase()) {
                            case "h1":
                                tokenHandler.push(new HeaderToken(1));
                                break;
                            case "h2":
                                tokenHandler.push(new HeaderToken(2));
                                break;
                            case "h3":
                                tokenHandler.push(new HeaderToken(3));
                                break;
                            case "h4":
                                tokenHandler.push(new HeaderToken(4));
                                break;
                            case "h5":
                                tokenHandler.push(new HeaderToken(5));
                                break;
                            case "li":
                                tokenHandler.push(new ListItemToken());
                                break;
                            case "bold":
                                tokenHandler.push(new BoldToken());
                                break;
                            default:
                                tokenHandler.push(new DefaultToken());
                                break;
                        }
                    } else {
                        while(!isClosingTagForMyself(tokenHandler.peek())) {
                            tokenHandler.peek().popped();
                            tokenHandler.pop();
                        }
                        tokenHandler.peek().popped();
                        tokenHandler.pop();
                        tokenHandler.peek().popped();
                        tokenHandler.pop();

                        tokenHandler.push(new DefaultToken());
                    }
                    break;
                case ' ':
                    tagNameComplete = true;
                    break;
                case '/':
                    if (!tagNameComplete) {
                        if (tagName.length() == 0) {
                            closingTag = true;
                        } else {
                            throw new IllegalStateException("Unexpected Character : " + theCurrentChar);
                        }
                    }
                    break;
                default:
                    if (!tagNameComplete) {
                        tagName+=theCurrentChar;
                    }

            }
        }

        @Override
        public void popped() {
        }
    }

    private final Stack<Token> tokenHandler;
    private final Output printer;

    public WordPressToAsciiDoctor(Output aPrinter) {
        printer = aPrinter;
        tokenHandler = new Stack<>();
        tokenHandler.push(new DefaultToken());
    }

    public void parse(String aContent) {
        for (int i=0;i<aContent.length();i++) {
            char theCurrentChar = aContent.charAt(i);
            tokenHandler.peek().process(theCurrentChar);
        }
    }
}
