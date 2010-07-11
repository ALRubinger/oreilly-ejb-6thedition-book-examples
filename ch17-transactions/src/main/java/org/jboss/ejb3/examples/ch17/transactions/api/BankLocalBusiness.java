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
package org.jboss.ejb3.examples.ch17.transactions.api;

import java.math.BigDecimal;

import org.jboss.ejb3.examples.ch17.transactions.entity.Account;

/**
 * Defines the contract for a bank
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface BankLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Constants --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * JNDI Name to which we'll bind
    */
   String JNDI_NAME = "BankLocalBusiness";

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Withdraws the specified amount from the account with 
    * the specified ID, returning the new balance.
    * @param amount
    * @throws IllegalArgumentException If the amount is not specified, the account
    * ID is not valid, or the amount to be withdrawn is less than 0
    * @throws InsufficientBalanceException If the amount to be withdrawn is greater
    * than the value of {@link Account#getBalance()}.
    */
   BigDecimal withdraw(long accountId, BigDecimal amount) throws IllegalArgumentException, InsufficientBalanceException;

   /**
    * Deposits the specified amount from the account with the
    * specified ID, returning the new balance.
    * @param amount
    * @throws IllegalArgumentException If the amount is not specified, the account
    * ID is not valid, or the amount to be deposited is less than 0
    */
   BigDecimal deposit(long accountId, BigDecimal amount) throws IllegalArgumentException;

   /**
    * Obtains the current balance from the account with the specified ID
    * @param accountId
    * @return
    * @throws IllegalArgumentException If the account ID is not valid
    */
   BigDecimal getBalance(long accountId) throws IllegalArgumentException;

   /**
    * Transfers the specified amount from one account to another
    * @param accountIdFrom The ID of the account from which we'll withdraw
    * @param accountIdTo The ID of the account to which we'll deposit
    * @param amount The amount to be transferred
    * @throws IllegalArgumentException If the amount is not specified, the amount is 
    *   less that 0, or either account ID is invalid
    * @throws InsufficientBalanceException If the amount is greater than the current 
    *   balance of the "from" account
    */
   void transfer(long accountIdFrom, long accountIdTo, BigDecimal amount) throws IllegalArgumentException,
         InsufficientBalanceException;

   /**
    * Transfers the specified amount from one account to another
    * @param accountFrom The account from which we'll withdraw
    * @param accountTo The account to which we'll deposit
    * @param amount The amount to be transferred
    * @throws IllegalArgumentException If the amount is not specified, the amount is 
    *   less that 0, or either account ID is invalid
    * @throws InsufficientBalanceException If the amount is greater than the current 
    *   balance of the "from" account
    */
   void transfer(Account accountFrom, Account accountTo, BigDecimal amount) throws IllegalArgumentException,
         InsufficientBalanceException;
}
