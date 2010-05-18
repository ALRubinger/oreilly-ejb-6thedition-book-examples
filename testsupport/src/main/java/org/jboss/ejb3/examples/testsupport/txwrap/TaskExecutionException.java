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
package org.jboss.ejb3.examples.testsupport.txwrap;

import javax.ejb.ApplicationException;
import javax.ejb.EJBException;

/**
 * Indicates that an exception has occurred while submitting
 * a task to {@link TxWrappingLocalBusiness#wrapInTx(java.util.concurrent.Callable...)}.
 * Not wrapped in {@link EJBException} because this is an {@link ApplicationException}
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@ApplicationException(rollback = true)
public class TaskExecutionException extends Exception
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
    * Creates a new instance with the specified root cause
    */
   public TaskExecutionException(final Throwable cause)
   {
      super(cause);
   }

   /**
    * Creates a new instance with the specified message
    * @param message
    */
   public TaskExecutionException(String message)
   {
      super(message);
   }

}
