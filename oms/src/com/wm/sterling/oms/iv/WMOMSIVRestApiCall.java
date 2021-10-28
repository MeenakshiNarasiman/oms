package com.wm.sterling.oms.iv;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.yantra.yfc.log.YFCLogCategory;
import com.yantra.yfs.japi.YFSEnvironment;
import com.yantra.yfs.japi.YFSException;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.wm.sterling.oms.constants.Constants;
import com.wm.sterling.oms.constants.WMApplicationConstants;
import com.wm.sterling.oms.util.WMCommonUtil;
import com.wm.sterling.oms.util.WMXMLUtil;

public class WMOMSIVRestApiCall implements Constants{

	private static final YFCLogCategory logger = YFCLogCategory.instance(WMOMSIVRestApiCall.class);

	public static String restAPICallToIV(String strUrl, String accessToken, JSONObject jsobj, YFSEnvironment env)
			throws IOException, YFSException, ParserConfigurationException, SAXException {
		String strResponse = null;
		URL url = new URL(strUrl);
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + accessToken);

			String requestBody = jsobj.toString();
			try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
				dos.write(requestBody.getBytes());
			}

			logger.debug("ResponseCode:" + connection.getResponseCode());
			StringBuilder content;
			String line;
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK
					|| connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					content = new StringBuilder();
					while ((line = br.readLine()) != null) {
						content.append(line);
					}
				}
				logger.debug("SuccessResponse:" + content);
				strResponse = content.toString();
				
			} else {
				try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
					content = new StringBuilder();
					while ((line = br.readLine()) != null) {
						content.append(line);
					}
				}
				logger.error("FailureResponse:" + content);
				String strError = content.toString();
				WMCommonUtil.invokeAPI(env, null, WMApplicationConstants.API_CREATE_EXCEPTION,
						getCreateExceptionDoc(jsobj, strError));
			}
			return strResponse;

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	private static Document getCreateExceptionDoc(JSONObject jsobj, String strError)
			throws ParserConfigurationException {

		Document createExceptionDoc = WMXMLUtil.getDocument();
		Element rootElement = createExceptionDoc.createElement(INBOX);
		rootElement.setAttribute(EXCEPTION_TYPE, EXCEPTION_TYPE_OMS_IV);
		rootElement.setAttribute(FLOW_NAME, FLOW_NAME_OMS_IV);
		rootElement.setAttribute(DESCRIPTION, jsobj.toString() + "--------" + strError);
		createExceptionDoc.appendChild(rootElement);
		if (logger.isDebugEnabled()) {
			logger.debug("SGTOMSIVRestApiCall.getCreateExceptionDoc Input:::::"
					+ XmlUtils.getString(createExceptionDoc));
		}
		return createExceptionDoc;

	}

}
