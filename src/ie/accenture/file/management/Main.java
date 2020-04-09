package ie.accenture.file.management;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.xml.sax.SAXException;

public class Main {
	
	private final static String PATH = "C:/management-system/";
	final static String DATE_FORMAT = "ddMMyy";
	private final static StringBuffer LOG_INVALID_FILE = new StringBuffer();
	private final static StringBuffer LOG_UNSUPPORTED_FILE_TYPES  = new StringBuffer();

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Hello File Management!");
	
		final File folder = new File(PATH);
		listFolderRecursive(folder);
		
		writeLogs();

	}
		
	private static void listFolderRecursive(File dir) throws ParserConfigurationException, SAXException, IOException {
		File[] subDirs =  dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		
		System.out.println("\nDirectory of " + dir.getAbsolutePath());
		listFilesForFolder(dir);
		
		for (File folder: subDirs) {
			listFolderRecursive(folder);
		}
	}
	
	public static void listFilesForFolder(final File folder) throws ParserConfigurationException, SAXException, IOException {
	    for (final File fileEntry : folder.listFiles()) {
	    	Pattern mypattern = Pattern.compile(".*\\.(doc|docx|PDF)", Pattern.CASE_INSENSITIVE);
	    	Matcher mymatcher= mypattern.matcher(fileEntry.getName());
	    	if (fileEntry.isDirectory()) {
	    		continue;
	    	}
	        if (!mymatcher.matches()) {
	        	LOG_UNSUPPORTED_FILE_TYPES.append(folder + "\\" + fileEntry.getName() + "|");
	        	continue;
	        }
	    	Pattern mypattern2 = Pattern.compile("^[a-zA-Z]{1,2}_[0-9]*_[0-9]{6}\\.(doc|docx|PDF)", Pattern.CASE_INSENSITIVE);
	    	Matcher mymatcher2 = mypattern2.matcher(fileEntry.getName());
	    	if (!mymatcher2.matches()) {	
	        	LOG_INVALID_FILE.append(folder + "\\" + fileEntry.getName() + "|");
	        	continue;
	        }
	    	
			BasicFileAttributes attr = Files.readAttributes(fileEntry.toPath(), BasicFileAttributes.class);

			System.out.println("creationTime: " + attr.creationTime());
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String dateCreated = df.format(attr.creationTime().toMillis());
			
			//System.out.println("dateCreated: " + dateCreated);
	        
	        String s1 = fileEntry.getName();
	        String[] fileName = s1.split("_");//splits the string based on string

	       String caseType = fileName[0];
	       String caseNumber = fileName[1];
	       String caseDate = fileName[2];
	       caseDate = caseDate.substring(0, 6); 
	       
	       if (!isDateValid(caseDate)) {
	    	   LOG_INVALID_FILE.append(folder + "\\" + fileEntry.getName() + "|");    	  
	    	   continue;
	       }

	        try {
	            File xmlFile = new File(fileEntry.getPath() + ".metadata.properties.xml");
	            if (!xmlFile.exists()) {
	              //addingXMLtoFile(xmlFile, caseType, caseNumber, caseDate);
	              //updateXML(PATH, fileEntry.getName(), caseType, caseNumber, caseDate);
	              updateXML(fileEntry.getPath(), caseType, caseNumber, caseDate, dateCreated);
	            } else {
	              System.out.println("File already exists.");
	            }
	          } catch (IOException e) {
	            System.out.println("An error occurred.");
	            e.printStackTrace();
	          }      
	    }
	       
	}

    public static boolean isDateValid(String date) {
        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
	public static Document readXML(String path) throws ParserConfigurationException, SAXException, IOException {
		// readin xml file - generic script
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// path to generic xml file
		Document doc = docBuilder.parse(path);
		return doc;
	}
    
	public static void updateXML(String main_path_filename, String caseType, String caseNumber, String caseDate, String dateCreated) throws ParserConfigurationException, SAXException, IOException {
		Document doc = readXML("C:\\Users\\camila.cardoso\\eclipse-workspace\\file-management-system\\src\\ie\\accenture\\file\\management\\generic.metadata.xml");
		// Get the root element
		// Node properties_ = doc.getFirstChild();
		NodeList listOfChildNodes = doc.getElementsByTagName("entry");
		// loop the entry child nodes
		for (int i = 0; i < listOfChildNodes.getLength(); i++) {
			Node node_ = listOfChildNodes.item(i);
			NamedNodeMap attr = node_.getAttributes();
			// System.out.println(node_.getTextContent());
			Node nodeAttr = attr.getNamedItem("key");
			if("cm:created".equals(nodeAttr.getTextContent())) {
				node_.setTextContent(dateCreated);
			}
			if ("acn:HCOCaseType".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(caseType);
			}
			if ("acn:HCOCaseNumber".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(caseNumber);
			}
			if ("acn:HCOCaseDate".equals(nodeAttr.getTextContent())) {
				// System.out.println("I am in");
				node_.setTextContent(caseDate);
			}
			// System.out.println(nodeAttr.getTextContent());
		}
		writeXML(main_path_filename, doc);
	}
    
	public static void writeXML(String path_filename, Document doc) {
		// write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		   
		    //Creating !DOCTYPE
		    DOMImplementation domImpl = doc.getImplementation();
		    DocumentType doctype = domImpl.createDocumentType("doctype",
		        null,
		        "http://java.sun.com/dtd/properties.dtd");
		    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
			
		    DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path_filename + ".metadata.properties.xml"));
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}
    
    public static void writeLogs() throws IOException {

    	 BufferedWriter bwr = new BufferedWriter(new FileWriter(new File(PATH + "LOG_INVALID_FILE")));
    	 
    	 //write contents of StringBuffer to a file
    	 bwr.write(LOG_INVALID_FILE.toString());
    	 
    	 //flush the stream
    	 bwr.flush();
    	 
    	 //close the stream
    	 bwr.close();
    	 
    	 BufferedWriter bwr2 = new BufferedWriter(new FileWriter(new File(PATH + "LOG_UNSUPPORTED_FILE_TYPES")));
    	 
    	 //write contents of StringBuffer to a file
    	 bwr2.write(LOG_UNSUPPORTED_FILE_TYPES.toString());
    	 
    	 //flush the stream
    	 bwr2.flush();
    	 
    	 //close the stream
    	 bwr2.close();
    }
    
//  public static void addingXMLtoFile(File fileXML, String caseType, String caseNumber, String caseDate) {
//	try {
//	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//
//	    //root elements
//	    Document doc = docBuilder.newDocument();
//
//	    Element rootElement = doc.createElement("properties");
//	    doc.appendChild(rootElement);
//	    
//	    rootElement.appendChild(createEntryElement("type", "cm:content", doc));
//	    rootElement.appendChild(createEntryElement("aspects", "cm:versionable", doc));
//	    rootElement.appendChild(createEntryElement("cm:title", "High Court Order Document", doc));
//	    rootElement.appendChild(createEntryElement("cm:description", "Uploaded via HCO Bulk Tool", doc)); 
//	    rootElement.appendChild(createEntryElement("cm:author", " ", doc));
//	    rootElement.appendChild(createEntryElement("cm:created", "2020-03-26", doc)); 
//	    rootElement.appendChild(createEntryElement("cm:owner", "Administrator", doc)); 
//	    rootElement.appendChild(createEntryElement("acn:HCOCaseType", caseType, doc));  
//	    rootElement.appendChild(createEntryElement("acn:HCOCaseNumber", caseNumber, doc));
//	    rootElement.appendChild(createEntryElement("acn:HCOCaseDate", caseDate, doc));
//
//	    //write the content into xml file
//	    TransformerFactory transformerFactory =  TransformerFactory.newInstance();
//	    Transformer transformer = transformerFactory.newTransformer();
//	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//	   
//	    //Creating !DOCTYPE
//	    DOMImplementation domImpl = doc.getImplementation();
//	    DocumentType doctype = domImpl.createDocumentType("doctype",
//	        null,
//	        "http://java.sun.com/dtd/properties.dtd");
//	    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
//	    
//	    //Saving XML to File
//	    DOMSource source = new DOMSource(doc);
//	    StreamResult result =  new StreamResult(fileXML);
//	    transformer.transform(source, result);
//
//	    System.out.println("Done");
//	
//	}catch(ParserConfigurationException pce){
//	    pce.printStackTrace();
//	}catch(TransformerException tfe){
//	    tfe.printStackTrace();
//	}
//}
//  private static Element createEntryElement(String key, String element, Document doc) {
//    
//	// entry content elements
//    Element entryContent = doc.createElement("entry");
//    entryContent.appendChild(doc.createTextNode(element));
//
//    //set attribute to staff element
//    Attr attr = doc.createAttribute("key");
//    attr.setValue(key);
//    entryContent.setAttributeNode(attr);
//    
//    return entryContent;
//}
    	
}
