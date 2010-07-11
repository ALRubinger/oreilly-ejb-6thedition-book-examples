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

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Represents an Employee with a series of properties to 
 * show JPA Mapping metadata.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class EmployeeWithProperties
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Primary key
    */
   @Id
   @GeneratedValue
   // Automatically manage PK creation for us
   private Long id;

   /**
    * Description of what the Employee's currently
    * working on.  We don't need to store this in the DB.
    */
   @Transient
   // Don't persist this
   private String currentAssignment;

   /**
    * Picture of the employee used in ID cards.
    */
   @Lob
   // Note that this is a binary large object
   @Basic(fetch = FetchType.LAZY, optional = true)
   // Don't load this by default; it's an expensive operation.  
   // Only load when requested. 
   private byte[] image;

   /**
    * Type of employee
    */
   @Enumerated(EnumType.STRING)
   // Show that this is an enumerated value, and the value to 
   // be put in the DB is the value of the enumeration toString().
   private EmployeeType type;

   /**
    * Date the employee joined the company
    */
   @Temporal(TemporalType.DATE)
   // Note that we should map this as an SQL Date field;
   // could also be SQL Time or Timestamp
   private Date since;

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the id
    */
   public Long getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(final Long id)
   {
      this.id = id;
   }

   /**
    * @return the currentAssignment
    */
   public String getCurrentAssignment()
   {
      return currentAssignment;
   }

   /**
    * @param currentAssignment the currentAssignment to set
    */
   public void setCurrentAssignment(final String currentAssignment)
   {
      this.currentAssignment = currentAssignment;
   }

   /**
    * @return the image
    */
   public byte[] getImage()
   {
      return image;
   }

   /**
    * @param image the image to set
    */
   public void setImage(final byte[] image)
   {
      this.image = image;
   }

   /**
    * @return the type
    */
   public EmployeeType getType()
   {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(final EmployeeType type)
   {
      this.type = type;
   }

   /**
    * @return the since
    */
   public Date getSince()
   {
      return since;
   }

   /**
    * @param since the since to set
    */
   public void setSince(final Date since)
   {
      this.since = since;
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
      return "EmployeeWithProperties [currentAssignment=" + currentAssignment + ", id=" + id + ", image="
            + Arrays.toString(image) + ", since=" + since + ", type=" + type + "]";
   }

}
