package de.mirkosertic.wordpressasciidoc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WordPressImport {

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

    public static void main(String[] aArgs)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParseException {
        DocumentBuilderFactory theFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder theBulder = theFactory.newDocumentBuilder();

        File theXMLFile = new File(System.getProperty("wordpressxmlfile"));

        Document theDocument = theBulder.parse(theXMLFile);

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
                    String theTitle = getCharacterDataFromElement(getChildWithName("title", theElement));
                    String thePostName = getCharacterDataFromElement(getChildWithName("wp:post_name", theElement));
                    String theGMTPostDate = getCharacterDataFromElement(getChildWithName("wp:post_date_gmt", theElement));
                    String theStatus = getCharacterDataFromElement(getChildWithName("wp:status", theElement));

                    System.out.println(theContent);

                    File theBaseDirectory = new File(theXMLFile.getParent(), "ascidoc");
                    theBaseDirectory.mkdirs();
                    File theFile = new File(theBaseDirectory, thePostName + ".adoc");

                    try (PrintWriter theWriter = new PrintWriter(new FileWriter(theFile))) {

                        theWriter.println("+++");

                        SimpleDateFormat thePostFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        thePostFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                        Date theDate = thePostFormat.parse(theGMTPostDate);

                        SimpleDateFormat theOutputFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");

                        StringBuilder theStringDate = new StringBuilder(theOutputFormat.format(theDate));
                        theStringDate.insert(theStringDate.length() -2 , ":");

                        theWriter.println("date = \"" + theStringDate+ "\"");
                        theWriter.println("title = \"" + theTitle + "\"");

                        theWriter.print("tags = [");

                        NodeList theTags = theElement.getElementsByTagName("category");
                        boolean hasTags = false;
                        for (int j=0;j<theTags.getLength();j++) {
                            Element theTagName = (Element) theTags.item(j);
                            if ("post_tag".equals(theTagName.getAttribute("domain"))) {
                                if (hasTags) {
                                    theWriter.print(", ");
                                }
                                theWriter.print("\"");
                                theWriter.print(getCharacterDataFromElement(theTagName));
                                theWriter.print("\"");
                                hasTags = true;
                            }
                        }

                        theWriter.println("]");


                        if (theStatus.contains("draft")) {
                            theWriter.println("draft = true");
                        } else {
                            theWriter.println("draft = false");
                        }


                        theWriter.println("+++");
                        theWriter.println();

                        StringOutput theOutput = new StringOutput();

                        WordPressToAsciiDoctor theParser = new WordPressToAsciiDoctor(theOutput);
                        theParser.parse(theContent);

                        // Unescape HTML entities
                        String theResult = theOutput.toString();
                        theResult = theResult.replace("&nbsp;", "");
                        theResult = theResult.replace("&lt;", "<");
                        theResult = theResult.replace("&gt;", ">");
                        theResult = theResult.replace("\n\n\n", "\n\n");
                        theWriter.print(theResult);
                    }
                }
            }
        }
    }
}
