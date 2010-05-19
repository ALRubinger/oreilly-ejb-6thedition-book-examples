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

import java.util.concurrent.Callable;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * EJB which wraps a specified series of {@link Callable}
 * tasks within the context of a new Transaction
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(TxWrappingLocalBusiness.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
// We always require a new Tx here, so we ensure to wrap 
public class TxWrappingBean implements TxWrappingLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.ejb.TxWrappingLocalBusiness#wrapInTx(java.util.concurrent.Callable<V>[])
    */
   @Override
   public void wrapInTx(final Callable<?>... tasks) throws IllegalArgumentException, TaskExecutionException
   {
      // Precondition check
      if (tasks == null)
      {
         throw new IllegalArgumentException("task must be specified");
      }

      // Just delegate along to the tasks in order; now it's executed inside of a Tx
      for (final Callable<?> task : tasks)
      {
         this.wrapInTx(task);
      }
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.testsupport.txwrap.TxWrappingLocalBusiness#wrapInTx(java.util.concurrent.Callable)
    */
   @Override
   public <T> T wrapInTx(final Callable<T> task) throws IllegalArgumentException, TaskExecutionException
   {
      try
      {
         // Invoke
         return task.call();
      }
      // Every problem we encounter here becomes an ApplicationException
      // to be unwrapped later by the caller (so the container doesn't wrap 
      // in EJBException
      catch (final Throwable e)
      {
         throw new TaskExecutionException(e);
      }
   }
}
