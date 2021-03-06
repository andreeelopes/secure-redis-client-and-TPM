package utils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class XMLParser {

	
	public static CipherConfig getClientconfig(String pathToConfigFile) {

		String cipherSuite = null;
		String macAlgorithm = null;
		String cipherProvider = null;
		String macProvider = null;
		String iv = null;
		int cipherKeySize = 0;
		int macKeySize = 0;
		
		try {
			File fXmlFile = new File(pathToConfigFile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			Element e = (Element) doc.getElementsByTagName("config").item(0);

			cipherSuite = e.getElementsByTagName("cipherSuite").item(0).getTextContent();
			macAlgorithm = e.getElementsByTagName("macAlgorithm").item(0).getTextContent();
			macProvider = e.getElementsByTagName("macProvider").item(0).getTextContent();
			cipherProvider = e.getElementsByTagName("cipherProvider").item(0).getTextContent();
			cipherKeySize = Integer.parseInt(e.getElementsByTagName("cipherKeySize").item(0).getTextContent());
			macKeySize = Integer.parseInt(e.getElementsByTagName("macKeySize").item(0).getTextContent());
			iv = e.getElementsByTagName("iv").item(0).getTextContent();

		} catch (Exception e) {
			e.printStackTrace();

		}

		return new CipherConfig(macAlgorithm, cipherSuite, macProvider,
				cipherProvider, cipherKeySize, macKeySize,iv);
	}
}
