package com.wm.sterling.oms.order.api;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.wm.sterling.oms.iv.WMOMSIVRestApiCall;
import com.wm.sterling.oms.constants.WMApplicationConstants;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;

public class WMCreateOrderReservation {
	
	private static final YFCLogCategory logger = YFCLogCategory.instance(WMCreateOrderReservation.class);
	
	private static final String TENANT_ID = YFSSystem.getProperty("TENANT_ID");
	private static final String CLIENT_ID = YFSSystem.getProperty("CLIENT_ID");
	private static final String CLIENT_SECRET = YFSSystem.getProperty("CLIENT_SECRET");
	
	public Document createOrderReservation(YFSEnvironment env, Document inDoc) throws IOException, YFSException, ParserConfigurationException, SAXException {
		logger.beginTimer("WMCreateOrderReservation.createOrderReservation : Begin");
		YFCElement eleInput = YFCDocument.getDocumentFor(inDoc).getDocumentElement();
		String strOrderNo = eleInput.getAttribute(WMApplicationConstants.ATTR_ORDER_NO);
		
		JSONObject finaljsobj = prepareCreateReservationRequest(eleInput, strOrderNo, env);
		
		logger.debug("JSONRequest for CreateRequest:::::"+finaljsobj);
		
		String accessToken = getAccessToken();
		logger.debug("AccessToken:::::"+accessToken);
		
		String strURL ="https://api.watsoncommerce.ibm.com/inventory/"+TENANT_ID+"/v1/reservations";
		logger.debug("Create-Reservation EndPoint URL:::::"+strURL);		
		
		String strResponse = WMOMSIVRestApiCall.restAPICallToIV(strURL,accessToken,finaljsobj,env);
		logger.debug("JSON Response from IV:::::"+strResponse);
				
		return null;
	}
	
	private JSONObject prepareCreateReservationRequest(YFCElement eleInput,String strOrderNo, YFSEnvironment env) {
		JSONObject jsobj;
        jsobj = new JSONObject();		
		JSONArray arr= new JSONArray();
	
		jsobj.put(WMApplicationConstants.ATTR_IV_REFERENCE, strOrderNo);
		jsobj.put(WMApplicationConstants.ATTR_IV_CONSIDER_SAFETYE_STOCK, "false");
		jsobj.put(WMApplicationConstants.ATTR_IV_DEMAND_TYPE, "OPEN_ORDER");
		jsobj.put(WMApplicationConstants.ATTR_IV_TIME_TO_EXPIRE, "60");
		JSONObject orderlineObj = new JSONObject();
		YFCNodeList<YFCElement> orderLinesNL = eleInput.getElementsByTagName("OrderLine");		
		for (YFCElement eachOrderLine : orderLinesNL) {			
			String strDeliveryMethod = eachOrderLine.getAttribute("DeliveryMethod");
			if(!YFCCommon.isVoid(strDeliveryMethod) && strDeliveryMethod.equalsIgnoreCase("PICK")) {
				String strPrimeLineNo = eachOrderLine.getAttribute("PrimeLineNo");
				String strOrderedQty = eachOrderLine.getAttribute("OrderedQty");
				String strShipNode = eachOrderLine.getAttribute("ShipNode");
				String strItemID = eachOrderLine.getChildElement("Item").getAttribute("ItemID");
				String strUnitOfMeasure = eachOrderLine.getChildElement("Item").getAttribute("UnitOfMeasure");
				
				orderlineObj.put(WMApplicationConstants.ATTR_IV_LINE_ID, strPrimeLineNo);
				orderlineObj.put(WMApplicationConstants.ATTR_IV_QUANTITY, strOrderedQty);
				orderlineObj.put(WMApplicationConstants.ATTR_IV_DELIVERY_METHOD, strDeliveryMethod);
				orderlineObj.put(WMApplicationConstants.ATTR_IV_ITEM_ID, strItemID);				
				orderlineObj.put(WMApplicationConstants.ATTR_IV_UNIT_OF_MEASURE, strUnitOfMeasure);
				orderlineObj.put(WMApplicationConstants.ATTR_IV_SHIP_NODE, strShipNode);				
			}				
		}
		
		arr.put(orderlineObj);	
		jsobj.put("lines",arr);     
		System.out.println(jsobj.toString(4));
		return jsobj;		
		
	}
	
	private static String getAccessToken() throws IOException {	
		
		String strUrl = "https://edge-api.watsoncommerce.ibm.com/inventory/" + TENANT_ID
				+ "/v1/configuration/oauth2/token";
		URL url = new URL(strUrl);
		String urlParameters = "grant_type=client_credentials&client_id=" + CLIENT_ID + "&client_secret="
				+ CLIENT_SECRET + "";
		byte[] postData = urlParameters.getBytes();
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
				dos.write(postData);
			}

			StringBuilder content;
			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				String line;
				content = new StringBuilder();
				while ((line = br.readLine()) != null) {
					content.append(line);
				}
			}
			// Parse to JSON and read access-token
			return content.substring(39, 71);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}	

}


