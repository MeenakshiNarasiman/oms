package com.wm.sterling.oms.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.yantra.interop.japi.YIFApi;
import com.yantra.interop.japi.YIFClientFactory;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class WMCommonUtil {

	private static YFCLogCategory logger = YFCLogCategory.instance(WMCommonUtil.class);

	public static YIFApi api;

	static {
		try {
			WMCommonUtil.api = YIFClientFactory.getInstance().getApi();
		} catch (Exception e) {
			throw new YFSException(e.getMessage());
		}
	}

	public static Document invokeAPI(YFSEnvironment env, Document template, String apiName, Document inDoc)
			throws YFSException, RemoteException {

		env.setApiTemplate(apiName, template);

		Document returnDoc = WMCommonUtil.api.invoke(env, apiName, inDoc);

		env.clearApiTemplate(apiName);
		return returnDoc;
	}
	
	/**
	 * Invokes a Sterling Commerce Service.
	 * 
	 * @param env
	 *            Sterling Commerce Environment Context.
	 * @param serviceName
	 *            Name of Service to invoke.
	 * @param inDoc
	 *            Input Document to be passed to the Service.
	 * @throws Exception
	 *             Exception thrown by the Service.
	 * @return Output of the Service.
	 * @throws RemoteException 
	 * @throws YFSException 
	 */
	public static Document invokeService(YFSEnvironment env, String serviceName, Document inDoc) throws YFSException, RemoteException {
		return WMCommonUtil.api.executeFlow(env, serviceName, inDoc);
	}
	
	
	 public static Document getDocument(StringBuilder content) throws ParserConfigurationException, SAXException, IOException {
		    String sCurrentXML = null;
		    if (content != null && content.length() > 0) {
		      sCurrentXML = content.toString();
		      if (sCurrentXML.startsWith("<")) {
		        StringReader strReader = new StringReader(sCurrentXML);
		        InputSource inpSource = new InputSource(strReader);
		        return getDocument(inpSource);
		      } 
		      
		      FileReader frIn = new FileReader(sCurrentXML);
		      Document docRetVal = null;
		      try {
		        InputSource inpSource = new InputSource(frIn);
		        docRetVal = getDocument(inpSource);
		      } finally {
		        frIn.close();
		      } 
		      return docRetVal;
		    } 
		    return null;
		  }
	
	 
	 public static Document getDocument(InputSource inpSource) throws ParserConfigurationException, SAXException, IOException {
		    DocumentBuilder dbdr = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    return dbdr.parse(inpSource);
		  }
	


}
