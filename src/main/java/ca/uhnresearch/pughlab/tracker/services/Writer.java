package ca.uhnresearch.pughlab.tracker.services;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import ca.uhnresearch.pughlab.tracker.dto.ViewDataResponse;

public interface Writer {
	Document getXMLDocument(ViewDataResponse data);
	
	void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory);
}
