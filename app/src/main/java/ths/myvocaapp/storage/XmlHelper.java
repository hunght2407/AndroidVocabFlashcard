package ths.myvocaapp.storage;

import android.content.ContextWrapper;
import android.os.Environment;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ths.myvocaapp.define.EVWord;

/**
 * Created by HungS7 on 1/28/2018.
 */

public class XmlHelper {
    private final static String PATH_XML = "Vocabulary_v2.xml";

    public static void loadVocabulary_v2(ContextWrapper context, LinkedList<EVWord> ouWordList) {
        ouWordList.clear();

        try {
            File inputFile = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()), PATH_XML);

            if (!inputFile.exists()) {
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList vocaGroup = doc.getElementsByTagName("word");

            for (int i = 0; i < vocaGroup.getLength(); i++) {
                Node vocaNode = vocaGroup.item(i);
                if (vocaNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element vocaElement = (Element) vocaNode;

                    String original 	= vocaElement.getElementsByTagName(EVWord.DF_ORIGINAL).item(0).getTextContent();
                    String phonetic 	= vocaElement.getElementsByTagName(EVWord.DF_PHONETIC).item(0).getTextContent();
                    String vietnamese 	= vocaElement.getElementsByTagName(EVWord.DF_VIETNAMESE).item(0).getTextContent();
                    String example 		= vocaElement.getElementsByTagName(EVWord.DF_EXAMPLE).item(0).getTextContent();

                    EVWord vocaTmp = new EVWord(original, phonetic, vietnamese, example);

                    if (!vocaElement.getElementsByTagName(EVWord.DF_FLAG).item(0).getTextContent().equals("0")) {
                        ouWordList.add(vocaTmp);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveVocabulary_v2(ContextWrapper context, LinkedList<EVWord> inWordList) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        if (inWordList.size() == 0)
            return;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            Element rootElement = doc.createElement("Vocabulary");

            doc.appendChild(rootElement);

            for (int i = 0; i < inWordList.size(); i++) {
                Element vocaElement = doc.createElement("word");

                vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_ORIGINAL, inWordList.get(i).getOriginal()));
                vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_PHONETIC, inWordList.get(i).getPhonetic()));
                vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_VIETNAMESE, inWordList.get(i).getVietnamese()));
                vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_EXAMPLE, inWordList.get(i).getExample()));

                if (inWordList.get(i).getFlag())
                    vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_FLAG, "1"));
                else
                    vocaElement.appendChild(getEmployeeElements(doc, EVWord.DF_FLAG, "0"));

                rootElement.appendChild(vocaElement);
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            File outputFile = new File( context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()), PATH_XML);
            StreamResult sfile = new StreamResult(outputFile);

            transformer.transform(source, sfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static EVWord getWordByIndex(ContextWrapper context, int index) {

        try {
            File inputFile = new File(context.getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()), PATH_XML);

            if (!inputFile.exists()) {
                return null;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
            NodeList vocaGroup = doc.getElementsByTagName("word");

            for (int i = index; i < vocaGroup.getLength(); i++) {
                Node vocaNode = vocaGroup.item(i);
                if (vocaNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element vocaElement = (Element) vocaNode;

                    String original 	= vocaElement.getElementsByTagName(EVWord.DF_ORIGINAL).item(0).getTextContent();
                    String phonetic 	= vocaElement.getElementsByTagName(EVWord.DF_PHONETIC).item(0).getTextContent();
                    String vietnamese 	= vocaElement.getElementsByTagName(EVWord.DF_VIETNAMESE).item(0).getTextContent();
                    String example 		= vocaElement.getElementsByTagName(EVWord.DF_EXAMPLE).item(0).getTextContent();

                    EVWord vocaTmp = new EVWord(original, phonetic, vietnamese, example);

                    if (!vocaElement.getElementsByTagName(EVWord.DF_FLAG).item(0).getTextContent().equals("0")) {
                        //ouWordList.add(vocaTmp);
                        return vocaTmp;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Node getEmployeeElements(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
}