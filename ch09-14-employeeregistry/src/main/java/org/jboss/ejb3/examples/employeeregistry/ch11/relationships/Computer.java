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
import javax.persistence.OneToOne;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;

/**
 * Represents an {@link Employee}'s computer.  The
 * relationship is bidirectional in the case the computer
 * is lost or in for servicing and needs to be returned.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class Computer extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Manufacturer of the computer
    */
   @Column(length = 100)
   // Length of VARCHAR
   private String make;

   /**
    * Model of the computer
    */
   @Column(length = 100)
   // Length of VARCHAR
   private String model;

   @OneToOne
   // Bidirectional relationship, mappedBy
   // is declared on the non-owning side
   private Employee owner;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the make
    */
   public String getMake()
   {
      return make;
   }

   /**
    * @param make the make to set
    */
   public void setMake(String make)
   {
      this.make = make;
   }

   /**
    * @return the model
    */
   public String getModel()
   {
      return model;
   }

   /**
    * @param model the model to set
    */
   public void setModel(String model)
   {
      this.model = model;
   }

   /**
    * @return the owner
    */
   public Employee getOwner()
   {
      return owner;
   }

   /**
    * @param owner the owner to set
    */
   public void setOwner(final Employee owner)
   {
      this.owner = owner;
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
      return Computer.class.getSimpleName() + " [make=" + make + ", model=" + model + ", owner=" + owner + ", getId()="
            + getId() + "]";
   }

}
