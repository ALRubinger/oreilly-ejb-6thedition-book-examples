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

import java.io.Serializable;

import javax.persistence.IdClass;

/**
 * Composite primary key class to be used as  
 * {@link IdClass} on {@link EmployeeWithExternalCompositePK}.
 * The instance members here will together compose 
 * an identity in the database (primary key).
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class ExternalEmployeePK implements Serializable
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Last Name
    */
   private String lastName;

   /**
    * Social Security Number (United States Federal ID)
    */
   private Long ssn;

   //-------------------------------------------------------------------------------------||
   // Functional Methods ----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the lastName
    */
   public String getLastName()
   {
      return lastName;
   }

   /**
    * @param lastName the lastName to set
    */
   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   /**
    * @return the ssn
    */
   public Long getSsn()
   {
      return ssn;
   }

   /**
    * @param ssn the ssn to set
    */
   public void setSsn(Long ssn)
   {
      this.ssn = ssn;
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
      result = prime * result + ((ssn == null) ? 0 : ssn.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ExternalEmployeePK other = (ExternalEmployeePK) obj;
      if (lastName == null)
      {
         if (other.lastName != null)
            return false;
      }
      else if (!lastName.equals(other.lastName))
         return false;
      if (ssn == null)
      {
         if (other.ssn != null)
            return false;
      }
      else if (!ssn.equals(other.ssn))
         return false;
      return true;
   }

}
