/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
  *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.examples.employeeregistry.ch11.relationships;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;

/**
 * Represents an Employee in the system.  Modeled as a simple
 * value object with some additional EJB and JPA annotations.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
// Mark that we're an Entity Bean, EJB's integration point
// with Java Persistence
public class Employee extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name
    */
   @Column(unique = true)
   // No two employees are to have the same name; not exactly
   // a real-world restriction, but shows usage. :)
   private String name;

   /**
    * The employee's address
    */
   @OneToOne
   @JoinColumn(name="ADDRESS_ID")
   // Unidirectional relationship
   private Address address;

   /**
    * The employee's computer
    */
   @OneToOne(mappedBy = "owner")
   // Bidirectional relationship
   private Computer computer;

   /**
    * Manager of the {@link Employee}
    */
   @ManyToOne
   private Employee manager;

   /**
    * {@link Employee}s reporting to this {@link Employee}
    */
   @OneToMany(mappedBy = "manager")
   private Collection<Employee> peons;

   /**
    * All {@link Phone}s for this {@link Employee}
    */
   @OneToMany
   // Unidirectional relationship
   private Collection<Phone> phones;

   /**
    * The {@link Team}s to which this {@link Employee} belongs
    */
   @ManyToMany(mappedBy = "members")
   private Collection<Team> teams;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Default constructor, required by JPA
    */
   public Employee()
   {
      peons = new ArrayList<Employee>();
      phones = new ArrayList<Phone>();
      teams = new ArrayList<Team>();
   }

   /**
    * Convenience constructor
    */
   public Employee(final String name)
   {
      this();
      // Set
      this.name = name;
   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(final String name)
   {
      this.name = name;
   }

   /**
    * @return the address
    */
   public Address getAddress()
   {
      return address;
   }

   /**
    * @param address the address to set
    */
   public void setAddress(final Address address)
   {
      this.address = address;
   }

   /**
    * @return the computer
    */
   public Computer getComputer()
   {
      return computer;
   }

   /**
    * @param computer the computer to set
    */
   public void setComputer(final Computer computer)
   {
      this.computer = computer;
   }

   /**
    * @return the manager
    */
   public Employee getManager()
   {
      return manager;
   }

   /**
    * @param manager the manager to set
    */
   public void setManager(final Employee manager)
   {
      this.manager = manager;
   }

   /**
    * @return the peons
    */
   public Collection<Employee> getPeons()
   {
      return peons;
   }

   /**
    * @param peons the peons to set
    */
   public void setPeons(final Collection<Employee> peons)
   {
      this.peons = peons;
   }

   /**
    * @return the teams
    */
   public Collection<Team> getTeams()
   {
      return teams;
   }

   /**
    * @param teams the teams to set
    */
   public void setTeams(final Collection<Team> teams)
   {
      this.teams = teams;
   }

   /**
    * @return the phones
    */
   public Collection<Phone> getPhones()
   {
      return phones;
   }

   /**
    * @param phones the phones to set
    */
   public void setPhones(final Collection<Phone> phones)
   {
      this.phones = phones;
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return Employee.class.getSimpleName() + " [name=" + name + ", getId()=" + getId() + "]";
   }

}
