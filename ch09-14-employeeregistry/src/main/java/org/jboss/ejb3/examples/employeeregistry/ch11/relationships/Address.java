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

import javax.persistence.Column;
import javax.persistence.Entity;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;

/**
 * Represents a simple Address.  Each {@link Employee} will
 * have one, though the relationship is not bidirectional.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
// Mark that we're an Entity Bean, EJB's integration point
// with Java Persistence
public class Address extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Street Address
    */
   @Column(length = 100)
   // Length of VARCHAR
   private String street;

   /**
    * City
    */
   @Column(length = 100)
   // Length of VARCHAR
   private String city;

   /**
    * Postal code of the state
    */
   @Column(length = 2)
   // Length of VARCHAR
   private String state;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Default constructor, required by JPA
    */
   public Address()
   {

   }

   /**
    * Convenience constructor
    */
   public Address(final String street, final String city, final String state)
   {
      // Set
      this.setStreet(street);
      this.setCity(city);
      this.setState(state);
   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the street
    */
   public String getStreet()
   {
      return street;
   }

   /**
    * @param street the street to set
    */
   public void setStreet(String street)
   {
      this.street = street;
   }

   /**
    * @return the city
    */
   public String getCity()
   {
      return city;
   }

   /**
    * @param city the city to set
    */
   public void setCity(String city)
   {
      this.city = city;
   }

   /**
    * @return the state
    */
   public String getState()
   {
      return state;
   }

   /**
    * @param state the state to set
    */
   public void setState(String state)
   {
      this.state = state;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return Address.class.getSimpleName() + " [city=" + city + ", state=" + state + ", street=" + street
            + ", getId()=" + getId() + "]";
   }
}
