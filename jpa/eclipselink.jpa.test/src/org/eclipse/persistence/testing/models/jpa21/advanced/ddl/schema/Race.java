/*******************************************************************************
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     02/04/2013-2.5 Guy Pelletier 
 *       - 389090: JPA 2.1 DDL Generation Support
 ******************************************************************************/   
package org.eclipse.persistence.testing.models.jpa21.advanced.ddl.schema;

import static javax.persistence.GenerationType.TABLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Converts;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.eclipse.persistence.testing.models.jpa21.advanced.converters.ResponsibilityConverter;

@Entity
@Table(name="JPA21_DDL_RACE", schema="THING")
public class Race {
    @Id
    @GeneratedValue(strategy=TABLE, generator="JPA21_RACE_GENERATOR")
    @TableGenerator(
        name="JPA21_RACE_GENERATOR", 
        table="SCHEMA_PK_SEQ",
        schema="GENERATOR",
        pkColumnName="SEQ_NAME", 
        valueColumnName="SEQ_COUNT",
        pkColumnValue="RACE_SEQ"
    )
    public Integer id;

    @Basic
    public String name;
    
    @ManyToMany(mappedBy="races")
    public List<Runner> runners;
    
    @OneToMany(mappedBy="race")
    @Converts({
        // Add this convert to avoid the auto apply setting to a Long.
        @Convert(attributeName="key.uniqueIdentifier", disableConversion=true),
        @Convert(attributeName="key.description", converter=ResponsibilityConverter.class)
    })  
    protected Map<Responsibility, Organizer> organizers;

    public Race() {
        runners = new ArrayList<Runner>();
        organizers = new HashMap<Responsibility, Organizer>();
    }

    public void addOrganizer(Organizer organizer, Responsibility responsibility) {
        organizers.put(responsibility, organizer);
    }
    
    public void addRunner(Runner runner) {
        runners.add(runner);
    }
    
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    
    public Map<Responsibility, Organizer> getOrganizers() {
        return organizers;
    }
    
    public List<Runner> getRunners() {
        return runners;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setOrganizers(Map<Responsibility, Organizer> organizers) {
        this.organizers = organizers;
    }
    
    public void setRunners(List<Runner> runners) {
        this.runners = runners;
    }
}
