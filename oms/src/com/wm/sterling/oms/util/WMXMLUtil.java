package com.wm.sterling.oms.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

import java.io.StringReader;



public class WMXMLUtil {

	/**
	 * Construct a document object.
	 * 
	 * @return empty document object
	 * @throws ParserConfigurationException for invalid format
	 */
	public static Document getDocument() throws ParserConfigurationException {
		// Create new document builder
		DocumentBuilder documentBuilder = getDocumentBuilder(false);
		return documentBuilder.newDocument();
	}

	/**
	 * Construct document builder object
	 * 
	 * @param requiredValidation indicating the parser needs to validate the
	 *                           document against a DTD or not.
	 * @return document builder object which can create new document
	 * @throws ParserConfigurationException if a DocumentBuilder cannot be created
	 *                                      which satisfies the configuration
	 *                                      requested.
	 */
	private static DocumentBuilder getDocumentBuilder(boolean requiredValidation) throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		if (requiredValidation) {
			documentBuilderFactory.setValidating(true);
		}
		return documentBuilderFactory.newDocumentBuilder();

	}
	
	public static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try  
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }

}
