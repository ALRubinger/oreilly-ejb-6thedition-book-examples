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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;

/**
 * Represents a Customer.  Each customer may have an {@link Employee}
 * which is the primary contact for the account, but the relationship
 * is unidirectional
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class Customer extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name
    */
   private String name;

   /**
    * The primary {@link Employee} contact for this {@link Customer}
    */
   @ManyToOne
   // Unidirectional
   private Employee primaryContact;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Default constructor, required by JPA
    */
   public Customer()
   {

   }

   /**
    * Convenience constructor
    */
   public Customer(final String name)
   {
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
    * @return the primaryContact
    */
   public Employee getPrimaryContact()
   {
      return primaryContact;
   }

   /**
    * @param primaryContact the primaryContact to set
    */
   public void setPrimaryContact(final Employee primaryContact)
   {
      this.primaryContact = primaryContact;
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
      return Customer.class.getSimpleName() + " [name=" + name + ", getId()=" + getId() + "]";
   }

}
