package ie.accenture.file.management;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
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
import org.w3c.dom.TypeInfo;

public class Main {
	
	private final static String PATH = "C:/management-system/";
	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	final static String DATE_FORMAT = "ddMMyy";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello File Management!");
		
		 fileLogConfig();
	
		final File folder = new File(PATH);
		listFilesForFolder(folder);

	}
	
	public static void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (Pattern.matches(".*\\.(xml|log|lck)", fileEntry.getName())) {
	        	continue;
	        }
	    	if (!Pattern.matches("^[a-zA-Z]{1,2}_[0-9]{4}_[0-9]{6}\\.doc", fileEntry.getName())) {
	        	LOGGER.log(Level.WARNING, "Ivalid file name pattern: " + folder + "\\" + fileEntry.getName()); 	
	        	continue;
	        }
	        
	        String s1 = fileEntry.getName();
	        String[] fileName = s1.split("_");//splits the string based on string

	       String caseType = fileName[0];
	       String caseNumber = fileName[1];
	       String caseDate = fileName[2];
	       caseDate = caseDate.substring(0, 6); 
	       
	       if (!isDateValid(caseDate)) {
	    	   LOGGER.log(Level.WARNING, "Ivalid date: " + folder + "\\" + fileEntry.getName());	    	  
	    	   continue;
	       }

	        try {
	            File xmlFile = new File(PATH + fileEntry.getName() + ".metadata.properties.xml");
	            if (xmlFile.createNewFile()) {
	              addingXMLtoFile(xmlFile, caseType, caseNumber, caseDate);
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
    
    public static void addingXMLtoFile(File fileXML, String caseType, String caseNumber, String caseDate) {
    	try {
    	    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    	    //root elements
    	    Document doc = docBuilder.newDocument();

    	    Element rootElement = doc.createElement("properties");
    	    doc.appendChild(rootElement);
    	    
    	    rootElement.appendChild(createEntryElement("type", "cm:content", doc));
    	    rootElement.appendChild(createEntryElement("aspects", "cm:versionable", doc));
    	    rootElement.appendChild(createEntryElement("cm:title", "High Court Order Document", doc));
    	    rootElement.appendChild(createEntryElement("cm:description", "Uploaded via HCO Bulk Tool", doc)); 
    	    rootElement.appendChild(createEntryElement("cm:author", " ", doc));
    	    rootElement.appendChild(createEntryElement("cm:created", "2020-03-26", doc)); 
    	    rootElement.appendChild(createEntryElement("cm:owner", "Administrator", doc)); 
    	    rootElement.appendChild(createEntryElement("acn:HCOCaseType", caseType, doc));  
    	    rootElement.appendChild(createEntryElement("acn:HCOCaseNumber", caseNumber, doc));
    	    rootElement.appendChild(createEntryElement("acn:HCOCaseDate", caseDate, doc));

    	    //write the content into xml file
    	    TransformerFactory transformerFactory =  TransformerFactory.newInstance();
    	    Transformer transformer = transformerFactory.newTransformer();
    	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    	   
    	    //Creating !DOCTYPE
    	    DOMImplementation domImpl = doc.getImplementation();
    	    DocumentType doctype = domImpl.createDocumentType("doctype",
    	        null,
    	        "http://java.sun.com/dtd/properties.dtd");
    	    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
    	    
    	    //Saving XML to File
    	    DOMSource source = new DOMSource(doc);
    	    StreamResult result =  new StreamResult(fileXML);
    	    transformer.transform(source, result);

    	    System.out.println("Done");
    	
    	}catch(ParserConfigurationException pce){
    	    pce.printStackTrace();
    	}catch(TransformerException tfe){
    	    tfe.printStackTrace();
    	}
    }
    
    private static Element createEntryElement(String key, String element, Document doc) {
	    
    	// entry content elements
	    Element entryContent = doc.createElement("entry");
	    entryContent.appendChild(doc.createTextNode(element));

	    //set attribute to staff element
	    Attr attr = doc.createAttribute("key");
	    attr.setValue(key);
	    entryContent.setAttributeNode(attr);
	    
	    return entryContent;
    }
    
    public static void fileLogConfig() {  

        FileHandler fh;  

        try {  

            // This block configure the logger with handler and formatter  
            fh = new FileHandler(PATH + "LogFileManagement.log");  
            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }

    }
    	
}
