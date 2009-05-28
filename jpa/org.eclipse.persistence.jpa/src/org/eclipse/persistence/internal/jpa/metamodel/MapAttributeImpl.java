/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/
package org.eclipse.persistence.internal.jpa.metamodel;

import java.util.Map;

import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.PluralAttribute.CollectionType;

import org.eclipse.persistence.internal.queries.MapContainerPolicy;
import org.eclipse.persistence.mappings.CollectionMapping;

/**
 * <p>
 * <b>Purpose</b>: Provides the implementation for the MapAttribute interface 
 *  of the JPA 2.0 Metamodel API (part of the JSR-317 EJB 3.1 Criteria API)
 * <p>
 * <b>Description</b>: 
 * 
 * @see javax.persistence.metamodel.MapAttribute
 * 
 * @since EclipseLink 2.0 - JPA 2.0
 *  
 * Contributors: 
 *     03/19/2009-2.0  dclarke  - initial API start    
 *     04/30/2009-2.0  mobrien - finish implementation for EclipseLink 2.0 release
 *       - 266912: JPA 2.0 Metamodel API (part of the JSR-317 EJB 3.1 Criteria API)  
 */ 
public class MapAttributeImpl<X, K, V> extends PluralAttributeImpl<X, java.util.Map<K, V>, V> implements MapAttribute<X, K, V> {

    private Type<K> keyType;

    protected MapAttributeImpl(ManagedTypeImpl<X> managedType, CollectionMapping mapping) {
        super(managedType, mapping);

        MapContainerPolicy policy = (MapContainerPolicy) mapping.getContainerPolicy();
        //Type<?> keyType = managedType.getMetamodel().getType(policy.getKeyType());
        Type<?> keyType = managedType.getMetamodel().getType(policy.getElementClass());
        this.keyType = (Type<K>) keyType;
    }

    public Class<K> getKeyJavaType() {
        //return ((MapContainerPolicy) getCollectionMapping().getContainerPolicy()).getKeyType();
        return ((MapContainerPolicy) getCollectionMapping().getContainerPolicy()).getContainerClass();
    }

    public Type<K> getKeyType() {
        return this.keyType;
    }

    public CollectionType getCollectionType() {
        return CollectionType.MAP;
    }
    
    public Class<Map<K, V>> getJavaType() {
        // TODO Auto-generated method stub
        return this.getMapping().getAttributeClassification();
    }
    
    public BindableType getBindableType() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String toString() {
        return "MapAttributeImpl[" + getMapping() + "]";
    }

    public Class<V> getBindableJavaType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAttribute() {
        // TODO Auto-generated method stub
        return false;
    }

}
