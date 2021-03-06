/*******************************************************************************
 * Copyright (C) 2013 Andrei Olaru.
 * 
 * This file is part of net.xqhs.XML.
 * 
 * net.xqhs.XML is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * net.xqhs.XML is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with net.xqhs.XML.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package XML;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;


import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import XML.XMLTree.XMLNode;
import XML.XMLTree.XMLNode.XMLAttribute;

/**
 * Executes the parsing of an XML file, using the SAX parser. Parsing should be done using one of the provided static
 * functions.
 * 
 * @author Andrei Olaru
 * 
 */
public class XMLParser extends DefaultHandler
{
	protected String	unitName		= null; // for logging
	//protected Logger	log				= null;
	
	private String		schemaFiles[]	= null;
	private XMLTree		thetree			= null;
	
	private InputStream schemaStream    = null;
	
	/**
	 * Validates and parses an XML file.
	 * 
	 * @param schema
	 *            The files containing the schemas for the XML file.
	 * @param file
	 *            The file to parse.
	 * 
	 * @return The {@link XMLTree} containing the information in the XML file.
	 */
	public static XMLTree validateParse(String schema, String file)
	{
		String schemas[] = new String[1];
		schemas[0] = schema;
		
		return validateParse(schemas, file);
	}
	
	
	
	/**
	 * Validates and parses an XML file.
	 * 
	 * @param schemas
	 *            The files containing the schemas for the XML file.
	 * @param file
	 *            The file to parse.
	 * @return The {@link XMLTree} containing the information in the XML file.
	 */
	public static XMLTree validateParse(String schemas[], String file)
	{
		XMLParser parser = new XMLParser(schemas);
		if(parser.validate(file))
		{
			XMLTree ret = parser.parseFile(file);
			parser.close();
			return ret;
		}
		return null;
	}
	
	
	public static XMLTree validateParse(InputStream schema, InputStream file)
	{
		XMLParser parser = new XMLParser(schema);
		if(parser.validate(file))
		{
			XMLTree ret = parser.parseFile(file);
			parser.close();
			return ret;
		}
		return null;
	}
	
	
	public static XMLTree parse(String file)
	{
		XMLParser parser = new XMLParser(new String[0]);
		XMLTree ret = parser.parseFile(file);
		parser.close();
		return ret;
	}
	
	public XMLParser(String schemas[])
	{
		this.schemaFiles = schemas;
		String names = "";
		for(String schema : schemas)
			names += schema.substring(schema.lastIndexOf('/') + 1) + "; ";
		this.unitName = this.getClass().getName() + ":" + names;
		// log = cm.getLogger();
		// log.setLevel(Level.INFO);
	}
	
	public XMLParser(InputStream schema){
		this.schemaStream = schema;
	}
	
	protected boolean validate(String file)
	{
		// build an XSD-aware SchemaFactory
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		// hook up org.xml.sax.ErrorHandler implementation.
		schemaFactory.setErrorHandler(this);
		
		// get the custom xsd schema describing the required format for my XML
		// files.
		Schema schemaXSD;
		try
		{
			Vector<Source> src = new Vector<Source>(schemaFiles.length);
			for(String schemaFile : schemaFiles)
			{
				File schema = new File(schemaFile);
				if(!schema.exists())
					try
					{
						throw new IOException("File does not exist [" + schemaFiles + "]");
					} catch(IOException e)
					{
						e.printStackTrace();
						return false;
					}
				src.add(new StreamSource(schema));
			}
			Source srcA[] = src.toArray(new Source[0]);
			schemaXSD = schemaFactory.newSchema(srcA);
		} catch(SAXException e)
		{
			e.printStackTrace();
			return false;
		}
		
		// Create a validator capable of validating XML files according to my
		// custom schema.
		Validator validator = schemaXSD.newValidator();
		
		// Get a parser capable of parsing vanilla XML into a DOM tree
		DocumentBuilder parser;
		try
		{
			DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			parser = fact.newDocumentBuilder();
		} catch(ParserConfigurationException e)
		{
			e.printStackTrace();
			return false;
		}
		
		// parse the XML purely as XML and get a DOM tree representation.
		Document document;
		try
		{
			document = parser.parse(new File(file));
			// parse the XML DOM tree against the stricter XSD schema
			validator.validate(new DOMSource(document));
		} catch(SAXException e)
		{
			e.printStackTrace();
			return false;
		} catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
		
	}
	
	protected boolean validate(InputStream xmlStream)
	{
		try {
			//SchemaFactory  factory = new XMLSchemaFactory();
			Source schemaFile = new StreamSource(this.schemaStream);
			Source xmlSource = new StreamSource(xmlStream);
			//Schema schema = factory.newSchema(schemaFile);
			//Validator validator = schema.newValidator();
			//validator.validate(xmlSource);
		//} catch (SAXException e) {
		//	return false;
		//} catch (IOException e) {
		//	return false;
		} catch (Exception e) {
			// Catches everything beyond: SAXException, and IOException.
			e.printStackTrace();
			return false;
		} catch (Error e) {
			// Needed this for debugging when I was having issues with my 1st set of code.
			e.printStackTrace();
			return false;
		}

		return true;
		
	}
	
	protected XMLTree parseFile(String fileName)
	{
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(fileName), this);
			
			XMLTree ret = thetree;
			thetree = null;
			return ret;
		} catch(ParserConfigurationException e)
		{
			e.printStackTrace();
		} catch(SAXException e)
		{
			e.printStackTrace();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	protected XMLTree parseFile(InputStream is)
	{
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(is, this);
			
			XMLTree ret = thetree;
			thetree = null;
			return ret;
		} catch(ParserConfigurationException e)
		{
			e.printStackTrace();
		} catch(SAXException e)
		{
			e.printStackTrace();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void startElement(String ns, String localName, String qName, Attributes att) throws SAXException
	{
		// String attr = "";
		// for(int i = 0; i < att.getLength(); i++)
		// attr += "[" + att.getQName(i) + " " + att.getValue(i) + "]";
		// log.trace("> " + ns + " | " + localName + " | " + qName + " | " +
		// attr);
		
		XMLNode node = new XMLNode(qName);
		
		if(thetree != null)
		{
			thetree.newNode(node);
		}
		else
		{
			thetree = new XMLTree(node);
		}
		
		for(int i = 0; i < att.getLength(); i++)
		{
			thetree.newAttribute(new XMLAttribute(att.getURI(i), att.getQName(i), att.getValue(i)));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		// log.trace("> " + uri + " | " + localName + " | " + qName + "//" +
		// thetree.currentNode().toString());
		thetree.finishNode();
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		String value = (new String(ch, start, length)).trim();
		// log.trace("value: [" + value + "]");
		
		if(!value.equals(""))
		{
			
			try
			{
				double dvalue = Double.parseDouble(value);
				// double dvalueRound = new
				// Double(Math.round(dvalue)).doubleValue() ;
				
				// if(dvalue != dvalueRound)
				// {
				thetree.currentNode().setValue(dvalue);
				// }
				// else{
				// thetree.currentNode().setValue(new
				// Long(Math.round(dvalue)).intValue());
				// }
			} catch(NumberFormatException ex)
			{
				// try
				// {
				// int nvalue = Integer.parseInt(value);
				// thetree.currentNode().setValue(nvalue);
				// } catch(NumberFormatException ex2)
				// {
				thetree.currentNode().setValue(value);
				// }
			}
		}
		
	}
	
	@Override
	public void startDocument() throws SAXException
	{
		// log.trace("startdoc");
	}
	
	@Override
	public void endDocument()
	{
		// if(thetree.currentNode() != null)
		// log.error("XML tree not consistent");
		// log.trace("enddoc");
	}
	
	public static void save(File file, XMLTree tree)
	{
		Document doc = tree.toDocument();
		
		Transformer transformer = null;
		try
		{
			transformer = TransformerFactory.newInstance().newTransformer();
			// transformer.setOutputProperty(OutputKeys.METHOD, "text");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			
			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(file);
			DOMSource source = new DOMSource(doc);
			try
			{
				transformer.transform(source, result);
			} catch(TransformerException e)
			{
				e.printStackTrace();
			}
		} catch(TransformerConfigurationException e)
		{
			e.printStackTrace();
		} catch(TransformerFactoryConfigurationError e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		// Log.exitLogger(unitName);
	}
	
}
