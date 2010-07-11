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
package org.jboss.ejb3.examples.ch19.timer.api;

import java.util.Date;
import java.util.List;

import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;

/**
 * Contract of a service capable of storing a series
 * of {@link CreditCardTransaction}s to be processed,
 * scheduling processing, and processing payment of
 * all pending transactions. 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface CreditCardTransactionProcessingLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Returns an immutable view of all transactions
    * pending processing
    * @return
    */
   List<CreditCardTransaction> getPendingTransactions();

   /**
    * Proceses all pending {@link CreditCardTransaction}s,
    * clearing them from the pending list when complete
    */
   void process();

   /**
    * Adds the specified {@link CreditCardTransaction} to be processed
    * @param transaction
    * @throws IllegalArgumentException If the transaction is null
    */
   void add(CreditCardTransaction transaction) throws IllegalArgumentException;

   /**
    * Schedules a new {@link Timer} to process pending payments
    * according to the supplied {@link ScheduleExpression}.  Returns
    * the {@link Date} representing when the next job is to fire.
    * @param expression
    * @return
    * @throws IllegalArgumentException If the expression is null
    */
   Date scheduleProcessing(ScheduleExpression expression) throws IllegalArgumentException;
}
