package com.wm.sterling.oms.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;


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
	
	/**
	 * Convert the XML document into XML string.
	 * 
	 * @param document input XML document
	 * @return output XML string
	 */
	public static String getXMLString(Document document) {
		return serialize(document);
	}
	
	/**
	 * Returns a formatted XML string for the Node, using encoding 'iso-8859-1'.
	 * 
	 * @param node a valid document object for which XML output in String form is required.
	 * @return the formatted XML string.
	 */
	public static String serialize(Node node) {
		return serialize(node, "iso-8859-1", true);
	}
	
	/**
	 * Return a XML string for a Node, with specified encoding and indenting flag.
	 * <p>
	 * <b>Note:</b> only serialize DOCUMENT_NODE, ELEMENT_NODE, and DOCUMENT_FRAGMENT_NODE
	 * 
	 * @param node the input node.
	 * @param encoding such as "UTF-8", "iso-8859-1"
	 * @param indenting indenting output or not.
	 * @return the XML string
	 */
	public static String serialize(Node node, String encoding, boolean indenting) {
		OutputFormat outFmt = null;
		StringWriter strWriter = null;
		XMLSerializer xmlSerializer = null;
		String retVal = null;

		try {
			outFmt = new OutputFormat("xml", encoding, indenting);
			outFmt.setOmitXMLDeclaration(true);
			strWriter = new StringWriter();
			xmlSerializer = new XMLSerializer(strWriter, outFmt);
			short ntype = node.getNodeType();

			switch (ntype) {
			case Node.DOCUMENT_FRAGMENT_NODE:
				xmlSerializer.serialize((DocumentFragment) node);
				break;
			case Node.DOCUMENT_NODE:
				xmlSerializer.serialize((Document) node);
				break;
			case Node.ELEMENT_NODE:
				xmlSerializer.serialize((Element) node);
				break;
			default:
				throw new IOException("Can serialize only Document, DocumentFragment and Element type nodes");
			}
			retVal = strWriter.toString();
		} catch (IOException e) {
			retVal = e.getMessage();
		} finally {
			try {
				if(strWriter!=null){
					strWriter.close();
				}
				
			} catch (IOException ie) {
				// Do nothing as getting error during string writer close operation
			}
		}

		return retVal;
	}

}
