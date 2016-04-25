import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.*;

public class LoadXML {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		/*EntityResolver resolver = new EntityResolver () {
			public InputSource resolveEntity (String publicId, String systemId) {
			String empty = "";
			ByteArrayInputStream bais = new ByteArrayInputStream(empty.getBytes());
			System.out.println("resolveEntity:" + publicId + "|" + systemId);
			return new InputSource(bais);
			}
			};
			//builder.setEntityResolver(resolver); */
		
		File inputFile = new File("testcase/xml/j_caesar.xml");
        DocumentBuilderFactory dbFactory= DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        //dBuilder.setEntityResolver(resolver);
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        System.out.println("----------------------------");
	}
}
