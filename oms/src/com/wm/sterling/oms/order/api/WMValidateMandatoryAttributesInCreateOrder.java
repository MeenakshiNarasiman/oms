package com.wm.sterling.oms.order.api;

import org.w3c.dom.Document;

import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.util.YFCCommon;
import com.yantra.yfc.util.YFCException;
import com.yantra.yfs.japi.YFSEnvironment;

public class WMValidateMandatoryAttributesInCreateOrder {

	public Document validateMandatoryAttributes(YFSEnvironment env, Document inDoc) {
		YFCElement eleInput = YFCDocument.getDocumentFor(inDoc).getDocumentElement();
		String strCustomeEmailId = eleInput.getChildElement("PersonInfoShipTo").getAttribute("EMailID");
		String strPaymentToken = eleInput.getChildElement("PaymentMethods").getChildElement("PaymentMethod")
				.getAttribute("PaymentReference2");
		if (YFCCommon.isVoid(strCustomeEmailId)) {
			throw new YFCException("Custome Email Id is null or empty");

		}
		if (YFCCommon.isVoid(strPaymentToken)) {
			throw new YFCException("Payment token is null or empty");

		}

		return inDoc;
	}

}
