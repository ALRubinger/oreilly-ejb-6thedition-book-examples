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
package org.jboss.ejb3.examples.employeeregistry.ch10.mapping;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * Represents an Employee in the system.  The identity
 * (primary key) is determined by embedded properties
 * via the {@link EmbeddedEmployeePK}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
// Mark that we're an Entity Bean, EJB's integration point
// with Java Persistence
public class EmployeeWithEmbeddedPK
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Primary key, composite and embedded
    */
   @EmbeddedId
   private EmbeddedEmployeePK id;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Default constructor, required by JPA
    */
   public EmployeeWithEmbeddedPK()
   {

   }

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the id
    */
   public EmbeddedEmployeePK getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(final EmbeddedEmployeePK id)
   {
      this.id = id;
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return EmployeeWithEmbeddedPK.class.getSimpleName() + " [id=" + id + "]";
   }

}
