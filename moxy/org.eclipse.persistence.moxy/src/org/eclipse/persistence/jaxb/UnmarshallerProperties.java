/*******************************************************************************
 * Copyright (c) 2012, 2014 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Blaise Doughan - 2.3.3 - initial implementation
 *     Marcel Valovy  - 2.6   - added case insensitive unmarshalling property
 *                            - added bean validation related properties
 ******************************************************************************/
package org.eclipse.persistence.jaxb;

/**
 * These are properties that may be set on an instance of Unmarshaller.  Below 
 * is an example of using the property mechanism to enable MOXy's JSON binding 
 * for an instance of Unmarshaller.
 * <pre>
 * Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 * unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
 * </pre>
 */
public class UnmarshallerProperties {

    /**
     * The name of the property used to specify a custom IDResolver class, to 
     * allow customization of ID/IDREF processing.
     * @since 2.3.3
     * @see org.eclipse.persistence.jaxb.IDResolver
     */
    public static final String ID_RESOLVER = "eclipselink.id-resolver";

    /**
     * The name of the property used to specify a value that will be prepended 
     * to all keys that are mapped to an XML attribute. By default there is no 
     * attribute prefix.  There is no effect when media type is 
     * "application/xml".  When this property is specified at the
     * <i>JAXBContext</i> level all instances of <i>Marshaller</i> and 
     * <i>Unmarshaller</i> will default to this attribute prefix.
     * @since 2.4
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties.JSON_ATTRIBUTE_PREFIX
     * @see org.eclipse.persistence.jaxb.MarshallerProperties.JSON_ATTRIBUTE_PREFIX
     */
    public static final String JSON_ATTRIBUTE_PREFIX = JAXBContextProperties.JSON_ATTRIBUTE_PREFIX;

    /**
     * The name of the property used to specify in the root node should be
     * included in the message (default is true). There is no effect when media
     * type is "application/xml".  When this property is specified at the
     * <i>JAXBContext</i> level all instances of <i>Marshaller</i> and 
     * <i>Unmarshaller</i> will default to this setting.
     * @since 2.4
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties.JSON_INCLUDE_ROOT
     * @see org.eclipse.persistence.jaxb.MarshallerProperties.JSON_INCLUDE_ROOT
     */
    public static final String JSON_INCLUDE_ROOT = JAXBContextProperties.JSON_INCLUDE_ROOT;

    /**
     * The Constant JSON_NAMESPACE_PREFIX_MAPPER. Provides a means to set a
     * a Map<String, String> of namespace URIs to prefixes.  Alternatively can
     * be an implementation of NamespacePrefixMapper.
     * @since 2.4
     * @see org.eclipse.persistence.oxm.NamespacePrefixMapper
     */
    public static final String JSON_NAMESPACE_PREFIX_MAPPER = JAXBContextProperties.NAMESPACE_PREFIX_MAPPER;

    /**
     * The name of the property used to specify the character (default is '.')
     * that separates the prefix from the key name. It is only used if namespace
     * qualification has been enabled be setting a namespace prefix mapper.  
     * When this property is specified at the <i>JAXBContext</i> level all 
     * instances of <i>Marshaller</i> and <i>Unmarshaller</i> will default to 
     * this setting.
     * @since 2.4
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties.NAMESPACE_SEPARATOR
     * @see org.eclipse.persistence.jaxb.MarshallerProperties.NAMESPACE_SEPARATOR
     */
    public static final String JSON_NAMESPACE_SEPARATOR  = "eclipselink.json.namespace-separator";

    /**
     * The name of the property used to specify the key that will correspond to
     * the property mapped with <i>@XmlValue</i>.  This key will only be used if
     * there are other mapped properties.  When this property is specified at 
     * the <i>JAXBContext</i> level all instances of <i>Marshaller</i> and 
     * <i>Unmarshaller</i> will default to this setting.
     * @since 2.4
     * @see org.eclipse.persistence.jaxb.JAXBContextPropertes.JSON_VALUE_WRAPPER
     * @see org.eclipse.persistence.jaxb.MarshallerPropertes.JSON_VALUE_WRAPPER
     */
    public static final String JSON_VALUE_WRAPPER = JAXBContextProperties.JSON_VALUE_WRAPPER;

    /**
     * The name of the property used to specify the type of binding to be 
     * performed.  When this property is specified at the <i>JAXBContext</i>
     * level all instances of <i>Marshaller</i> and <i>Unmarshaller</i> will
     * default to this media type. Supported values are:
     * <ul>
     * <li>MediaType.APPLICATION_XML (default)
     * <li>MediaType.APPLICATION_JSON
     * <li>"application/xml"
     * <li>"application/json"
     * </ul>
    * @since 2.4
    * @see org.eclipse.persistence.jaxb.JAXBContextProperties.MEDIA_TYPE
    * @see org.eclipse.persistence.jaxb.MarshallerProperties.MEDIA_TYPE
    * @see org.eclipse.persistence.oxm.MediaType
    */
    public static final String MEDIA_TYPE = JAXBContextProperties.MEDIA_TYPE;

    /**
     * The name of the property used to specify if the media type should be  
     * auto detected (default is false).  Only set to true when the media type
     * is unknown.  Otherwise set the MEDIA_TYPE property.   If the type can not
     * be auto-detected an unmarshal with the MEDIA_TYPE value will be performed. 
     * @since 2.4
     * @see org.eclipse.persistence.jaxb.UnmarshallerProperties.MEDIA_TYPE
     * @see org.eclipse.persistence.oxm.MediaType
     */
    public static final String AUTO_DETECT_MEDIA_TYPE = "eclipselink.auto-detect-media-type";

    
    public static final String OBJECT_GRAPH = "eclipselink.object-graph";

    /**
     * The Constant JSON_WRAPPER_AS_ARRAY_NAME. If true the grouping 
     * element will be used as the JSON key. There is no effect when media type 
     * is "application/xml".  When this property is specified at the
     * <i>JAXBContext</i> level all instances of <i>Marshaller</i> and 
     * <i>Unmarshaller</i> will default to this.
     * 
     * <p><b>Example</b></p>
     * <p>Given the following class:</p>
     * <pre>
     * &#64;XmlAccessorType(XmlAccessType.FIELD)
     * public class Customer {
     * 
     *     &#64;XmlElementWrapper(name="phone-numbers")
     *     &#64;XmlElement(name="phone-number")
     *     private List<PhoneNumber> phoneNumbers;
     * 
     * }
     * </pre>
     * <p>If the property is set to false (the default) the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : {
     *         "phone-number" : [ {
     *             ...
     *         }, {
     *             ...
     *         }]
     *     }
     * }
     * </pre>
     * <p>And if the property is set to true, then the JSON output will be:</p>
     * <pre>
     * {
     *     "phone-numbers" : [ {
     *         ...
     *     }, {
     *         ...
     *     }]
     * }
     * </pre>
     * @since 2.4.2
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME
     * @see org.eclipse.persistence.jaxb.MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME
     */
    public static final String JSON_WRAPPER_AS_ARRAY_NAME = JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME;

    /**
     * If set to <i>Boolean.TRUE</i>, {@link org.eclipse.persistence.jaxb.JAXBUnmarshaller} will match
     * XML Elements and XML Attributes to Java fields case insensitively.
     *
     * <p><b>Example</b></p>
     * <p>Given the following class:</p>
     * <pre>
     * &#64;XmlAccessorType(XmlAccessType.FIELD)
     * public class Customer {
     *
     *     &#64;XmlElement
     *     private String name;
     *     &#64;XmlAttribute
     *     private int id;
     *
     * }
     * </pre>
     * <p>If the property is set to true, the following XML object will match the class and will be unmarshaled.</p>
     * <pre>
     * &lt;customer iD="007"&gt;
     *   &lt;nAMe&gt;cafeBabe&lt;/nAMe&gt;
     * &lt;/customer&gt;
     * </pre>
     *
     * <p><b>By default, case-insensitive unmarshalling is turned off.</b><p/>
     *
     * <p>The property can be set through {@link org.eclipse.persistence.jaxb.JAXBUnmarshaller#setProperty(String, Object)}.</p>
     *
     * @since 2.6.0
     *
     * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=331241">EclipseLink Forum, Bug 331241.</a>
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#UNMARSHALLING_CASE_INSENSITIVE
     */
    public static final String UNMARSHALLING_CASE_INSENSITIVE = JAXBContextProperties.UNMARSHALLING_CASE_INSENSITIVE;

    /**
     * Property for setting bean validation mode.
     * Valid values {@link BeanValidationMode#AUTO} (default),{@link BeanValidationMode#CALLBACK}, {@link BeanValidationMode#NONE}.
     *
     * @since 2.6
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#BEAN_VALIDATION_MODE
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#BEAN_VALIDATION_MODE
     */
    public static final String BEAN_VALIDATION_MODE = JAXBContextProperties.BEAN_VALIDATION_MODE;

    /**
     * Property for setting preferred or custom validator factory. Must implement javax.validation.ValidatorFactory.
     *
     * @since 2.6
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#BEAN_VALIDATION_FACTORY
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#BEAN_VALIDATION_FACTORY
     */
    public static final String BEAN_VALIDATION_FACTORY = JAXBContextProperties.BEAN_VALIDATION_FACTORY;

    /**
     * Property for setting bean validation target groups. Must be of type Class<?>[].
     *
     * @since 2.6
     * @see org.eclipse.persistence.jaxb.JAXBContextProperties#BEAN_VALIDATION_GROUPS
     * @see org.eclipse.persistence.jaxb.MarshallerProperties#BEAN_VALIDATION_GROUPS
     */
    public static final String BEAN_VALIDATION_GROUPS = JAXBContextProperties.BEAN_VALIDATION_GROUPS;

}