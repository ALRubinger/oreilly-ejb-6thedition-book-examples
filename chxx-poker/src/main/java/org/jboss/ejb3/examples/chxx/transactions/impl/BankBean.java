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
package org.jboss.ejb3.examples.chxx.transactions.impl;

import java.math.BigDecimal;
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.api.InsufficientBalanceException;
import org.jboss.ejb3.examples.chxx.transactions.entity.Account;

/**
 * The bank with which users and the Poker provider 
 * may interact with underlying accounts.  For instance users
 * may wish to make cash deposits into their personal account, 
 * or the Poker provider may transfer money from the user's account
 * to the poker account when the user places a bet.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(BankLocalBusiness.class)
@LocalBinding(jndiBinding = BankLocalBusiness.JNDI_NAME)
public class BankBean implements BankLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(BankBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * JPA hook
    */
   @PersistenceContext
   private EntityManager em;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness#deposit(long, java.math.BigDecimal)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   // Default Tx Attribute; create a new Tx if not present, else use the existing
   public BigDecimal deposit(long accountId, final BigDecimal amount) throws IllegalArgumentException
   {
      // Get the account
      final Account account = this.getAccount(accountId);

      // Deposit
      return account.deposit(amount);

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness#getBalance(long)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.SUPPORTS)
   // Don't require a Tx is in play, but respect a currently-operating 
   // one so we get the correct visibility from inside the Tx
   public BigDecimal getBalance(long accountId) throws IllegalArgumentException
   {
      // Get the account
      final Account account = this.getAccount(accountId);

      // Return the current balance
      return account.getBalance();
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness#transfer(long, long, java.math.BigDecimal)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   // Default Tx Attribute; create a new Tx if not present, else use the existing
   public void transfer(long accountIdFrom, long accountIdTo, BigDecimal amount) throws IllegalArgumentException,
         InsufficientBalanceException
   {
      // Get the accounts in question
      final Account accountFrom = this.getAccount(accountIdFrom);
      final Account accountTo = this.getAccount(accountIdTo);

      // Withdraw (which will throw InsufficientBalance if that's the case)
      accountFrom.withdraw(amount);

      // And put the money into the new account
      accountTo.deposit(amount);
      log.info("Deposited " + amount + " to " + accountTo + " from " + accountFrom);

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness#withdraw(long, java.math.BigDecimal)
    */
   @Override
   @TransactionAttribute(TransactionAttributeType.REQUIRED)
   // Default Tx Attribute; create a new Tx if not present, else use the existing
   public BigDecimal withdraw(long accountId, BigDecimal amount) throws IllegalArgumentException,
         InsufficientBalanceException
   {
      // Get the account
      final Account account = this.getAccount(accountId);

      // Withdraw
      return account.withdraw(amount);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the {@link Account} with the specified ID
    * 
    * @throws IllegalArgumentException If the ID does not represent a valid Account
    */
   private Account getAccount(final long accountId) throws IllegalArgumentException
   {
      // Get the account
      final Account account;
      try
      {
         account = em.find(Account.class, new Long(accountId));
      }
      // Translate the exception; we were given a bad input
      catch (final EntityNotFoundException enfe)
      {
         throw new IllegalArgumentException("Could not find account with ID " + accountId);
      }

      // Return
      return account;
   }
}
