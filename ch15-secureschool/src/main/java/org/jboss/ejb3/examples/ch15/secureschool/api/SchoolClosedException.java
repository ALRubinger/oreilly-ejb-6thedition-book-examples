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
package org.jboss.ejb3.examples.ch15.secureschool.api;

import javax.ejb.ApplicationException;
import javax.ejb.EJBAccessException;

import org.jboss.ejb3.examples.ch15.secureschool.impl.Roles;

/**
 * Thrown when a user in role other than {@link Roles#ADMIN}
 * attempts to open the front door to school while it's closed
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ApplicationException(rollback = true)
// So this isn't wrapped in EJBException
public class SchoolClosedException extends EJBAccessException
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * serialVersionUID
    */
   private static final long serialVersionUID = 1L;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new exception
    */
   private SchoolClosedException(final String message)
   {
      super(message);
   }

   //-------------------------------------------------------------------------------------||
   // Factory ----------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructs a new exception with the specified, required message
    * @param message
    * @throws IllegalArgumentException If the message is not specified
    */
   public static SchoolClosedException newInstance(final String message) throws IllegalArgumentException
   {
      // Precondition checks
      if (message == null)
      {
         throw new IllegalArgumentException("message must be specified");
      }

      // Return
      return new SchoolClosedException(message);
   }

}
