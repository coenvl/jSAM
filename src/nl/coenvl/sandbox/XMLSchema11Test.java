/**
 * File XMLSchema11Test.java
 *
 * This file is part of the jSAM project.
 * 
 * Copyright 2014 Coen van Leeuwen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package nl.coenvl.sandbox;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XMLSchema11Test
 * 
 * In package nl.coenvl.sandbox
 * 
 * @author leeuwencjv
 * @version 0.1
 * @since 4 feb. 2014
 * 
 */
public class XMLSchema11Test implements ErrorHandler {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out
					.println("Usage: java -jar XMLSchema11Test.jar xmlfile xsdscheme");
			System.exit(1);
		}

		XMLSchema11Test test = new XMLSchema11Test();

		String xmlfile = args[0];
		String schemapath = args[1];

		System.exit(test.validate(xmlfile, schemapath));
	}

	private int error = 0;

	@Override
	public void error(SAXParseException exception) throws SAXException {
		error++;
		System.out.println(exception.getSystemId() + ":"
				+ exception.getLineNumber() + ":" + exception.getColumnNumber()
				+ ":" + exception.getMessage());
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		error++;
		System.out.println(exception.getSystemId() + ":"
				+ exception.getLineNumber() + ":" + exception.getColumnNumber()
				+ ":" + exception.getMessage());
	}

	public int validate(String xmlfile, String schemapath) {
		try {
			System.setProperty(
					"javax.xml.validation.SchemaFactory:http://www.w3.org/2001/XMLSchema/v1.1",
					"org.apache.xerces.jaxp.validation.XMLSchema11Factory");
			SchemaFactory sf = SchemaFactory
					.newInstance("http://www.w3.org/2001/XMLSchema/v1.1");
			Schema s = sf.newSchema(new StreamSource(schemapath));
			Validator v = s.newValidator();
			v.setErrorHandler(this);
			v.validate(new StreamSource(xmlfile));
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
		return error;
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		error++;
		System.out.println(exception.getSystemId() + ":"
				+ exception.getLineNumber() + ":" + exception.getColumnNumber()
				+ ":" + exception.getMessage());
	}
}