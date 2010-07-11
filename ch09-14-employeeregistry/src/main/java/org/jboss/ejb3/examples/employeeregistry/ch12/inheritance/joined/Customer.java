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
package org.jboss.ejb3.examples.employeeregistry.ch12.inheritance.joined;

import javax.persistence.Entity;

/**
 * Represents a customer, a {@link Person}
 * associated with a company.  Sits in the middle of an inheritance
 * hierarchy and is extended by employee types, who are a special type of 
 * {@link Customer}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity(name = "JOINED_CUSTOMER")
public class Customer extends Person
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Street-level address
    */
   private String street;

   /**
    * City
    */
   private String city;

   /**
    * State
    */
   private String state;

   /**
    * ZIP
    */
   private String zip;

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
   public void setStreet(final String street)
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
   public void setCity(final String city)
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
   public void setState(final String state)
   {
      this.state = state;
   }

   /**
    * @return the zip
    */
   public String getZip()
   {
      return zip;
   }

   /**
    * @param zip the zip to set
    */
   public void setZip(final String zip)
   {
      this.zip = zip;
   }

}
