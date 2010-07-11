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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.jboss.ejb3.examples.testsupport.entity.AutogenIdentityBase;
import org.jboss.ejb3.examples.testsupport.entity.IdentityBase;

/**
 * Represents an Employee in the system.  Inherits the
 * primary key support from {@link IdentityBase#getId()}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
// Mark that we're an Entity Bean, EJB's integration point
// with Java Persistence
@Table(name = "employees_with_autogen_pk")
// Explicitly denote the name of the table in the DB
public class EmployeeWithMappedSuperClassId extends AutogenIdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name of the employee
    */
   // We can use @Column.name to denote the name of the column in the DB 
   @Column(name = "employee_name")
   private String name;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Default constructor, required by JPA
    */
   public EmployeeWithMappedSuperClassId()
   {

   }

   /**
    * Convenience constructor
    */
   public EmployeeWithMappedSuperClassId(final String name)
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
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return EmployeeWithMappedSuperClassId.class.getSimpleName() + " [id=" + this.getId() + ", name=" + name + "]";
   }
}
