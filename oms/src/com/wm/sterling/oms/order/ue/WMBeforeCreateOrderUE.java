package com.wm.sterling.oms.order.ue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.wm.sterling.oms.constants.Constants;
import com.wm.sterling.oms.constants.WMApplicationConstants;
import com.wm.sterling.oms.iv.WMOMSIVRestApiCall;
import com.wm.sterling.oms.util.WMXMLUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNodeList;
import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.core.YFSSystem;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;

public class WMBeforeCreateOrderUE implements Constants{
	private static final YFCLogCategory logger = YFCLogCategory.instance(WMBeforeCreateOrderUE.class);
	private static final String TENANT_ID = YFSSystem.getProperty("TENANT_ID");
	private static final String CLIENT_ID = YFSSystem.getProperty("CLIENT_ID");
	private static final String CLIENT_SECRET = YFSSystem.getProperty("CLIENT_SECRET");

	/**
	 * This Method is used to invoke the service for BeforeCreateOrderUE Implementation
	 * @param env   Environment
	 * @param docIN input doc
	 * @return createOrder input doc after modification
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws YFSException
	 */

	public Document beforeCreateOrder(YFSEnvironment env, Document inDoc)
			throws YFSException, IOException, ParserConfigurationException, SAXException {
		logger.beginTimer("WMBeforeCreateOrderUE.createOrderReservation : Begin");
		inDoc = validateMandatoryAttributes(env, inDoc);

		inDoc = createOrderReservation(env, inDoc);
		logger.endTimer("WMBeforeCreateOrderUE.createOrderReservation : End");
		logger.verbose("beforeCreateOrderUE createOrderInput is:-" + WMXMLUtil.getXMLString(inDoc));
		return inDoc;
	}

	/**
	 * This Method is used to create IV reservation and stamp the reservation
	 * information in orderline element 
	 * @param env   Environment
	 * @param docIN input doc
	 * @return createOrder input doc after modification
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws YFSException
	 * 
	 */

	private Document createOrderReservation(YFSEnvironment env, Document inDoc)
			throws IOException, YFSException, ParserConfigurationException, SAXException {
		logger.beginTimer("WMBeforeCreateOrderUE.createOrderReservation : Begin");
		YFCElement eleInput = YFCDocument.getDocumentFor(inDoc).getDocumentElement();
		String strOrderNo = eleInput.getAttribute(WMApplicationConstants.ATTR_ORDER_NO);

		JSONObject finaljsobj = prepareCreateReservationRequest(eleInput, strOrderNo, env);

		logger.debug("JSONRequest for CreateRequest:::::" + finaljsobj);

		String accessToken = getAccessToken();
		logger.debug("AccessToken:::::" + accessToken);

		String strURL = "https://api.watsoncommerce.ibm.com/inventory/" + TENANT_ID + "/v1/reservations";
		logger.debug("Create-Reservation EndPoint URL:::::" + strURL);

		String strResponse = WMOMSIVRestApiCall.restAPICallToIV(strURL, accessToken, finaljsobj, env);
		logger.debug("JSON Response from IV:::::" + strResponse);
		if (!YFCCommon.isVoid(strResponse)) {

			inDoc = stampReservationId(strResponse, inDoc);

		}
		logger.endTimer("WMBeforeCreateOrderUE.createOrderReservation : End");
		return inDoc;
	}

	/**
	 * Method is used to validate the mandatory attributes and throws exception if
	 * attributes are null or empty
	 *
	 * @param env   Environment
	 * @param docIN input doc
	 * @return createOrder input doc after modification
	 * 
	 */

	private Document validateMandatoryAttributes(YFSEnvironment env, Document inDoc) {
		logger.beginTimer("WMBeforeCreateOrderUE.validateMandatoryAttributes : Begin");
		YFCElement eleInput = YFCDocument.getDocumentFor(inDoc).getDocumentElement();
		String strCustomeEmailId = eleInput.getChildElement(E_PERSON_INFO_SHIP_TO).getAttribute(ATTR_EMAIL_ID);
		String strPaymentToken = eleInput.getChildElement(E_PAYMENT_METHODS).getChildElement(E_PAYMENT_METHOD)
				.getAttribute(ATTR_PAYMENT_REFERENCE_2);
		if (YFCCommon.isVoid(strCustomeEmailId)) {
			throw new YFCException("Customer Email Id is null or empty");

		}
		if (YFCCommon.isVoid(strPaymentToken)) {
			throw new YFCException("Payment token is null or empty");

		}
		logger.endTimer("WMBeforeCreateOrderUE.validateMandatoryAttributes : End");
		return inDoc;
	}

	/* This method will prepare the reservation request */

	private JSONObject prepareCreateReservationRequest(YFCElement eleInput, String strOrderNo, YFSEnvironment env) {
		logger.beginTimer("WMBeforeCreateOrderUE.prepareCreateReservationRequest : Begin");
		JSONObject jsobj;
		jsobj = new JSONObject();
		JSONArray arr = new JSONArray();

		jsobj.put(WMApplicationConstants.ATTR_IV_REFERENCE, strOrderNo);
		jsobj.put(WMApplicationConstants.ATTR_IV_CONSIDER_SAFETYE_STOCK, false);
		jsobj.put(WMApplicationConstants.ATTR_IV_DEMAND_TYPE, "OPEN_ORDER");
		jsobj.put(WMApplicationConstants.ATTR_IV_TIME_TO_EXPIRE, YFSSystem.getProperty("IV_RES_EXPIRATION_TIME"));		
		YFCNodeList<YFCElement> orderLinesNL = eleInput.getElementsByTagName(E_ORDER_LINE);
		for (YFCElement eachOrderLine : orderLinesNL) {
			String strDeliveryMethod = eachOrderLine.getAttribute(ATTR_DELIVERY_METHOD);
			if (!YFCCommon.isVoid(strDeliveryMethod) && strDeliveryMethod.equalsIgnoreCase(VAL_DELIVERY_METHOD_PICK)) {
				String strPrimeLineNo = eachOrderLine.getAttribute(ATTR_PRIME_LINE_NO);
				String strOrderedQty = eachOrderLine.getAttribute(ATTR_ORDERED_QTY);
				String strShipNode = eachOrderLine.getAttribute(ATTR_SHIP_NODE);
				String strItemID = eachOrderLine.getChildElement(E_ITEM).getAttribute(ATTR_ITEM_ID);
				String strUnitOfMeasure = eachOrderLine.getChildElement(E_ITEM).getAttribute(ATTR_UNIT_OF_MEASURE);
				JSONObject orderlineObj = new JSONObject();
				orderlineObj.put(ATTR_IV_LINE_ID, strPrimeLineNo);
				orderlineObj.put(ATTR_IV_QUANTITY, strOrderedQty);
				orderlineObj.put(ATTR_IV_DELIVERY_METHOD, strDeliveryMethod);
				orderlineObj.put(ATTR_IV_ITEM_ID, strItemID);
				orderlineObj.put(ATTR_IV_UNIT_OF_MEASURE, strUnitOfMeasure);
				orderlineObj.put(ATTR_IV_SHIP_NODE, strShipNode);
				arr.put(orderlineObj);
			}
		}		
		jsobj.put(ATTR_IV_LINES, arr);
		System.out.println(jsobj.toString(4));
		logger.endTimer("WMBeforeCreateOrderUE.prepareCreateReservationRequest : End");
		return jsobj;

	}

	/**
	 * This method is used to generate the authorization token for IV
	 * 
	 * @param env   Environment
	 * @param docIN input doc
	 * @return authorization token
	 * 
	 */

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

	/**
	 * This method is used to stamp the reservation detials
	 * 
	 * @param env   Environment
	 * @param docIN input doc
	 * @return createOrderInput
	 * 
	 */
	public Document stampReservationId(String strResponse, Document docIn) {
		logger.beginTimer("WMBeforeCreateOrderUE.stampReservationId : Begin");
		YFCElement eleInput = YFCDocument.getDocumentFor(docIn).getDocumentElement();
		JSONObject jsobj = new JSONObject(strResponse);
		JSONArray jsarr = jsobj.getJSONArray(ATTR_IV_LINES);

		YFCNodeList<YFCElement> orderLineNL = eleInput.getElementsByTagName(E_ORDER_LINE);
		for (YFCElement eachOrderLine : orderLineNL) {
			String primeLineNo = eachOrderLine.getAttribute(ATTR_PRIME_LINE_NO);
			for (int i = 0; i < jsarr.length(); i++) {
				JSONObject inobj = jsarr.getJSONObject(i);
				String strLineId = inobj.getString(ATTR_IV_LINE_ID);
				Double reservedQty = (Double) inobj.get(ATTR_IV_RESERVED_QTY);
				if (reservedQty > 0) {
					String strReservedQty = Double.toString(reservedQty);
					String strIVReservationID = inobj.getString(ATTR_IV_RESERVATION_ID);
					if (primeLineNo.equals(strLineId)) {
						YFCElement orderLineReservation = eachOrderLine.createChild(E_ORDER_LINE_RESERVATIONS)
								.createChild(E_ORDER_LINE_RESERVATION);
						orderLineReservation.setAttribute(ATTR_ITEM_ID,
								eachOrderLine.getChildElement(E_ITEM).getAttribute(ATTR_ITEM_ID));
						orderLineReservation.setAttribute(ATTR_NODE, eachOrderLine.getAttribute(ATTR_SHIP_NODE));
						orderLineReservation.setAttribute(ATTR_RESERVATION_ID, strIVReservationID);
						orderLineReservation.setAttribute(ATTR_UNIT_OF_MEASURE,
								eachOrderLine.getChildElement(E_ITEM).getAttribute(ATTR_UNIT_OF_MEASURE));
						orderLineReservation.setAttribute(ATTR_QUANTITY, strReservedQty);
					}
				}
			}

		}

		logger.endTimer("WMBeforeCreateOrderUE.stampReservationId : End");
		return docIn;
	}

}
