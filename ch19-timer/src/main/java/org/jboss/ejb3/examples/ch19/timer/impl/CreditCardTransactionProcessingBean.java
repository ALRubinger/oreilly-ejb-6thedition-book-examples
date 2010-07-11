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
package org.jboss.ejb3.examples.ch19.timer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.ScheduleExpression;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransaction;
import org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransactionProcessingLocalBusiness;

/**
 * Implementation of a Service capable of storing pending
 * {@link CreditCardTransaction}s for later processing.
 * These may either be processed via a business vall to
 * {@link CreditCardTransactionProcessingLocalBusiness#process()}
 * or via any number of configured timers using the EJB Timer Service.
 * At deployment, a default timer will be set to run
 * every hour on the hour (as configured by the {@link Schedule}
 * annotation atop {@link CreditCardTransactionProcessingBean#processViaTimeout(Timer)}.
 * The {@link CreditCardTransactionProcessingBean#scheduleProcessing(ScheduleExpression)}
 * method shows programmatic creation of timers given a supplied
 * {@link ScheduleExpression}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Singleton
@Local(CreditCardTransactionProcessingLocalBusiness.class)
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class CreditCardTransactionProcessingBean implements CreditCardTransactionProcessingLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CreditCardTransactionProcessingBean.class.getName());

   /**
    * Wildcard denoting "all" in timer expressions
    */
   private static final String EVERY = "*";

   /**
    * Timer value denoting 0
    */
   private static final String ZERO = "0";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@link SessionContext} hook to the EJB Container;
    * from here we may obtain a {@link TimerService} via
    * {@link SessionContext#getTimerService()}.
    */
   @Resource
   private SessionContext context;

   /**
    * We can directly inject the {@link TimerService} as well.
    */
   @Resource
   @SuppressWarnings("unused")
   // Just for example
   private TimerService timerService;

   /**
    * {@link List} of all pending transactions.  Guarded
    * by the concurrency policies of this EJB.
    */
   private final List<CreditCardTransaction> pendingTransactions = new ArrayList<CreditCardTransaction>();

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Timeout
   // Mark this method as the EJB timeout method for timers created programmatically.  If we're
   // just creating a timer via @Schedule, @Timer is not required.
   @Schedule(dayOfMonth = EVERY, month = EVERY, year = EVERY, second = ZERO, minute = ZERO, hour = EVERY)
   // This timeout will be created on deployment and fire every hour on the hour; declarative creation
   @Lock(LockType.WRITE)
   public void processViaTimeout(final Timer timer)
   {
      // Just delegate to the business method
      this.process();
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransactionProcessingLocalBusiness#add(org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransaction)
    */
   @Override
   @Lock(LockType.WRITE)
   public void add(final CreditCardTransaction transaction) throws IllegalArgumentException
   {
      // Precondition check
      if (transaction == null)
      {
         throw new IllegalArgumentException("transaction must be specified");
      }

      // Add
      this.pendingTransactions.add(transaction);
      log.info("Added transaction pending to be processed: " + transaction);
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransactionProcessingLocalBusiness#getPendingTransactions()
    */
   @Override
   @Lock(LockType.READ)
   public List<CreditCardTransaction> getPendingTransactions()
   {
      // Return immutable so callers can't modify our internal state
      return Collections.unmodifiableList(pendingTransactions);
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransactionProcessingLocalBusiness#process()
    */
   @Override
   @Lock(LockType.WRITE)
   public void process()
   {
      // Process all pending transactions
      for (final CreditCardTransaction transaction : pendingTransactions)
      {
         // Fake it, we're not really gonna 
         // charge you in the EJB Book examples!
         log.info("Processed transaction: " + transaction);
      }

      // Clear the pending payments as we've "charged" all now
      pendingTransactions.clear();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch19.timer.api.CreditCardTransactionProcessingLocalBusiness#scheduleProcessing(javax.ejb.ScheduleExpression)
    */
   @Override
   public Date scheduleProcessing(final ScheduleExpression expression) throws IllegalArgumentException
   {
      // Precondition checks
      if (expression == null)
      {
         throw new IllegalArgumentException("Timer expression must be specified");
      }

      // Programmatically create a new Timer from the given expression via the TimerService from the SessionContext
      final TimerService timerService = context.getTimerService();
      final Timer timer = timerService.createCalendarTimer(expression);
      final Date next = timer.getNextTimeout();
      log.info("Created " + timer + " to process transactions; next fire is at: " + timer.getNextTimeout());
      return next;
   }
}
