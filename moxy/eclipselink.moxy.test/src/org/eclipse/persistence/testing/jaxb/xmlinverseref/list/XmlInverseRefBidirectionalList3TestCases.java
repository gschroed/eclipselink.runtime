/*******************************************************************************
 * Copyright (c) 2011, 2012 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Denise Smith, February 2013
 ******************************************************************************/
package org.eclipse.persistence.testing.jaxb.xmlinverseref.list;

import java.util.ArrayList;

import org.eclipse.persistence.testing.jaxb.JAXBWithJSONTestCases;

public class XmlInverseRefBidirectionalList3TestCases extends JAXBWithJSONTestCases{
	private final static String XML_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlinverseref/bidirectionalList3.xml";
    private final static String JSON_RESOURCE = "org/eclipse/persistence/testing/jaxb/xmlinverseref/bidirectionalList3.json";
    
	public XmlInverseRefBidirectionalList3TestCases(String name) throws Exception {
		super(name);
		setControlJSON(JSON_RESOURCE);
		setControlDocument(XML_RESOURCE);
		setClasses(new Class[]{Person.class});
	}

	@Override
	protected Object getControlObject() {
		Person p = new Person();
		p.name = "theName";
		Address addr = new Address();
		addr.street = "theStreet";
		addr.owner = p;
		
		Address addr2 = new Address();
		addr2.street = "theStreet2";
		addr2.owner = p;
		p.addrs = new ArrayList<Address>();
		p.addrs.add(addr);
		p.addrs.add(addr2);
		return addr2;
	}

	
}
