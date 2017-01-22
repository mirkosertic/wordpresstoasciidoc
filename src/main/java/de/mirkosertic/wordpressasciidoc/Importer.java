package de.mirkosertic.wordpressasciidoc;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;

public class Importer {

    private static Element getChildWithName(String aName, Element aElement) {
        NodeList theChilds = aElement.getElementsByTagName(aName);
        if (theChilds.getLength() == 1) {
            return (Element) theChilds.item(0);
        }
        return null;
    }

    public static String getCharacterDataFromElement(Element aElement) {
        NodeList theNodeList = aElement.getChildNodes();
        String theData;

        for(int index = 0; index < theNodeList.getLength(); index++){
            if(theNodeList.item(index) instanceof CharacterData){
                CharacterData child = (CharacterData) theNodeList.item(index);
                theData = child.getData();

                if(theData != null && theData.trim().length() > 0)
                    return child.getData();
            }
        }
        return "";
    }

    public static void main(String[] aArgs) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory theFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder theBulder = theFactory.newDocumentBuilder();
        Document theDocument = theBulder.parse(new File("D:\\Mirko\\ownCloud\\mirkoserticde.wordpress.2017-01-22.xml"));

        XPathFactory theXPathFactory = XPathFactory.newInstance();
        XPath theXPath = theXPathFactory.newXPath();
        XPathExpression theExpression = theXPath.compile("/rss/channel/item");

        int k=0;
        NodeList theItems = (NodeList) theExpression.evaluate(theDocument, XPathConstants.NODESET);
        for (int i=0;i<theItems.getLength();i++) {
            Element theElement = (Element) theItems.item(i);
            Element thePostType = getChildWithName("wp:post_type", theElement);
            if (thePostType != null) {
                String thePostingType = getCharacterDataFromElement(thePostType);
                if ("post".endsWith(thePostingType)) {
                    String theContent = getCharacterDataFromElement(getChildWithName("content:encoded", theElement));

                    System.out.println(theContent);

                    k++;
                }
            }
        }

        System.out.println(k);
    }
}
