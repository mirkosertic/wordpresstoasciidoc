package de.mirkosertic.wordpressasciidoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class WordPressToAsciiDoctor {

    public interface Token {
        void process(char theCurrentChar);

        void popped();
    }

    private class DefaultToken implements Token {

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

    private class HeaderToken extends DefaultToken {

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

    private class ListItemToken extends DefaultToken {

        @Override
        protected void doFirstRun() {
            printer.print(" * ");
        }

        @Override
        public void popped() {
            printer.println();
        }
    }

    private class BoldToken extends DefaultToken {

        @Override
        protected void doFirstRun() {
            printer.print("*");
        }

        @Override
        public void popped() {
            printer.print("*");
        }
    }

    private class LinkToken extends DefaultToken {

        private final String linkTarget;

        public LinkToken(String aTarget) {
            linkTarget = aTarget;
        }

        @Override
        protected void doFirstRun() {
            printer.print(linkTarget);
            printer.print("[");
        }

        @Override
        public void popped() {
            printer.print("] ");
        }
    }

    private class ImageToken extends DefaultToken {

        private final String src;
        private final String width;
        private final String height;

        public ImageToken(String aSrc, String aWidth, String aHeight) {
            src = aSrc;
            width = aWidth;
            height = aHeight;
        }

        @Override
        protected void doFirstRun() {
            printer.print("image:");
            printer.print(src);
        }

        @Override
        public void popped() {
            printer.print("[");;
            if (width != null && height != null) {
                printer.print(width);
                printer.print(",");
                printer.print(height);
            }
            printer.print("]");
        }
    }

    private class UnorderedListToken extends DefaultToken {

        public UnorderedListToken() {
            printer.println();
        }

        @Override
        public void popped() {
            printer.println();
            printer.println();
        }
    }

    public class PreformattedToken extends DefaultToken {

        private final String clazz;

        public PreformattedToken(String aClazz) {
            clazz = aClazz;
        }

        @Override
        protected void doFirstRun() {
            if (clazz != null) {
                if (clazz.contains("lang:java")) {
                   printer.println();
                   printer.print("[source,java]");
                   printer.println();
                   printer.print("----");
                   printer.println();
                }
            }
        }

        @Override
        public void popped() {
            if (clazz != null) {
                if (clazz.contains("lang:java")) {
                    printer.println();
                    printer.print("----");
                    printer.println();
                }
            }
        }
    }

    private class HTMLToken implements Token {

        private final Map<String, String> attributes;

        private String tagName;
        private boolean tagNameComplete;
        private boolean closingTag;

        private boolean inAttributeValue;
        private boolean inEscaping;
        private String currentAttributeName;
        private String currentAttributeValue;

        public HTMLToken() {
            tagName = "";
            attributes = new HashMap<>();
            currentAttributeName = "";
            currentAttributeValue = "";
        }

        private boolean isClosingTagForMyself(Token aHandler) {
            if (aHandler instanceof HTMLToken) {
                HTMLToken theHTML = (HTMLToken) aHandler;
                return tagName.equals(theHTML.tagName) && theHTML.closingTag;
            }
            return false;
        }

        @Override
        public void process(char theCurrentChar) {
            switch (theCurrentChar) {
                case '>':
                    tagNameComplete = true;
                    if (currentAttributeName.length() > 0) {
                        attributes.put(currentAttributeName, currentAttributeValue);
                        currentAttributeName = "";
                        currentAttributeValue = "";
                    }
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
                            case "ul":
                                tokenHandler.push(new UnorderedListToken());
                                break;
                            case "li":
                                tokenHandler.push(new ListItemToken());
                                break;
                            case "bold":
                                tokenHandler.push(new BoldToken());
                                break;
                            case "a":
                                tokenHandler.push(new LinkToken(attributes.get("href")));
                                break;
                            case "pre":
                                tokenHandler.push(new PreformattedToken(attributes.get("class")));
                                break;
                            case "img":
                                ImageToken theToken = new ImageToken(attributes.get("src"), attributes.get("width"), attributes.get("height"));
                                theToken.doFirstRun();
                                theToken.popped();

                                tokenHandler.peek().popped();
                                tokenHandler.pop();

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
                    if (!tagNameComplete) {
                        tagNameComplete = true;
                    } else {
                        if (inEscaping) {
                            currentAttributeValue+=' ';
                        } else {
                            attributes.put(currentAttributeName, currentAttributeValue);
                            inAttributeValue = false;
                            currentAttributeName = "";
                            currentAttributeValue = "";
                        }
                    }
                    break;
                case '=':
                    if (inEscaping) {
                        currentAttributeValue+='=';
                    } else {
                        inAttributeValue = true;
                    }
                    break;
                case '/':
                    if (!tagNameComplete) {
                        if (tagName.length() == 0) {
                            closingTag = true;
                        } else {
                            throw new IllegalStateException("Unexpected Character : " + theCurrentChar);
                        }
                    } else {
                        if (inEscaping) {
                            currentAttributeValue += '/';
                        } else {
                            if (!inAttributeValue) {
                                currentAttributeName += theCurrentChar;
                            }
                        }
                    }
                    break;
                case '\"':
                    if (!inEscaping) {
                        inEscaping = true;
                    } else {
                        inEscaping = false;
                    }
                    break;
                default:
                    if (!tagNameComplete) {
                        tagName+=theCurrentChar;
                    } else {
                        if (inAttributeValue) {
                            currentAttributeValue+=theCurrentChar;
                        } else {
                            currentAttributeName+=theCurrentChar;
                        }
                    }
                    break;
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

/*


<table class="inline">
<thead>
<tr class="row0">
<th class="col0 rightalign font-secondary">Measurement</th>
<th class="col1 rightalign font-secondary">GWT 2.8.0</th>
<th class="col3 rightalign font-secondary">TeaVM 0.4.3</th>
<th class="col3 rightalign font-secondary">TeaVM 1.0.0-SNAPSHOT</th>
</tr>
</thead>
<tbody>
<tr class="row1">
<th class="col0 rightalign font-secondary">Compile time(OPTIMIZER ENABLED)</th>
<td class="col1 rightalign">43.777 ms</td>
<td class="col3 rightalign">10.383 ms</td>
<td class="col3 rightalign">12.244 ms</td>
</tr>
<tr class="row2">
<th class="col0 rightalign font-secondary">Size of JS in unoptimized mode</th>
<td class="col1 rightalign">3.700 kb</td>
<td class="col3 rightalign">2.890 kb</td>
<td class="col3 rightalign">2.650 kb</td>
</tr>
<tr class="row3">
<th class="col0 rightalign font-secondary">Size of JS in optimized mode</th>
<td class="col1 rightalign">1.540 kb</td>
<td class="col3 rightalign">955 kb</td>
<td class="col3 rightalign">1013 kb</td>
</tr>
</tbody>
</table>


 */


