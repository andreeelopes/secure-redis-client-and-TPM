package client;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {


	public static String[] getClientconfig() {

		String cipherSuite = null;
		String macAlgorithm = null;
		String cipherProvider = null;
		String macProvider = null;
		String macKHashAlgorithm = null;
		String pwdHashAlgorithm = null;
		String cipherKeySize= null;
		String macKeySize= null;
		try {
			File fXmlFile = new File("client.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();

			Element e = (Element) doc.getElementsByTagName("config").item(0);

			cipherSuite = e.getElementsByTagName("cipherSuite").item(0).getTextContent();
			macAlgorithm = e.getElementsByTagName("macAlgorithm").item(0).getTextContent();
			macProvider = e.getElementsByTagName("macProvider").item(0).getTextContent();
			cipherProvider = e.getElementsByTagName("cipherProvider").item(0).getTextContent();
			cipherKeySize = e.getElementsByTagName("cipherKeySize").item(0).getTextContent();
			macKeySize =e.getElementsByTagName("macKeySize").item(0).getTextContent();
			
		} catch (Exception e) {
			e.printStackTrace();

		}

		return new String[] {cipherSuite, macAlgorithm, cipherProvider, macProvider,cipherKeySize,macKeySize};
	}
}
