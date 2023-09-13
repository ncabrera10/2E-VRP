package util;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;


/**
 * Implements a simple XML parser that builds a JDOM document in memory
 * @author Jorge E. Mendoza (dev@jorge-mendoza.com)
 * @since Aug 20, 2016
 *
 *
 */
public class XMLParser {

	/**
	 * 
	 * @param file the file to parse
	 * @return a JDOM document representation of the parsed file
	 */
	public static Document parse(String file){
		SAXBuilder builder = new SAXBuilder();
		File xmlFile = new File(file);
		Document document=null;
		try {
			document = (Document) builder.build(xmlFile);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return document;
	}
}
