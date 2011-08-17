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
package org.jboss.ejb3.examples.ch17.transactions.impl;

import java.math.BigDecimal;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.ejb3.examples.ch17.transactions.api.BankLocalBusiness;
import org.jboss.ejb3.examples.ch17.transactions.api.BlackjackGameLocalBusiness;
import org.jboss.ejb3.examples.ch17.transactions.api.InsufficientBalanceException;
import org.jboss.ejb3.examples.ch17.transactions.entity.Account;
import org.jboss.ejb3.examples.ch17.transactions.entity.User;

/**
 * Implementation of a service capable of placing single 
 * bets upon a blackjack game.  Though the gameplay itself is not
 * modeled, its inputs and outputs are done transactionally.
 * Each game is to take place in its own Tx, suspending
 * an existing Tx if one is in play.  This is to ensure
 * that the output of each game is committed (you win or lose)
 * regardless of if an error occurrs later within the caller's Tx.
 * Once your money's on the table, there's no going back! :)
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
@Local(BlackjackGameLocalBusiness.class)
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
// Each game must be in a new Tx, suspending the existing enclosing Tx if necessary;
// At the class-level, this annotation now applied to all methods
public class BlackjackGameBean implements BlackjackGameLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Zero value used for comparison
    */
   private static final BigDecimal ZERO = new BigDecimal(0);

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Hook to JPA
    */
   @PersistenceContext
   private EntityManager em;

   /**
    * The bank service which will handle account transfers
    * during win/lose
    */
   @EJB
   private BankLocalBusiness bank;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   /**
    * @see org.jboss.ejb3.examples.ch17.transactions.api.BlackjackGameLocalBusiness#bet(long, java.math.BigDecimal)
    */
   @Override
   public boolean bet(final long userId, final BigDecimal amount) throws IllegalArgumentException,
         InsufficientBalanceException
   {
      // Precondition checks
      if (userId < 0)
      {
         throw new IllegalArgumentException("userId must be valid (>0)");
      }
      if (amount == null)
      {
         throw new IllegalArgumentException("amount must be specified");
      }
      if (amount.compareTo(ZERO) < 0)
      {
         throw new IllegalArgumentException("amount must be greater than 0");
      }

      // Check the balance of the user account
      final Account userAccount = em.find(User.class, new Long(userId)).getAccount();
      final BigDecimal currentBalanceUserAccount = userAccount.getBalance();
      if (amount.compareTo(currentBalanceUserAccount) > 0)
      {
         throw new InsufficientBalanceException("Cannot place bet of " + amount + " when the user account has only "
               + currentBalanceUserAccount);
      }

      // Fake the game logic and just determine if the user wins
      final boolean win = Math.random() > 0.5;

      // Get the Poker Service account (assume we always have enough to back our bet, these are just tests :))
      final Account blackjackServiceAccount = em.find(Account.class, BlackjackServiceConstants.ACCOUNT_BLACKJACKGAME_ID);

      // Transfer the money based upon the outcome
      if (win)
      {
         bank.transfer(blackjackServiceAccount, userAccount, amount);
      }
      else
      {
         bank.transfer(userAccount, blackjackServiceAccount, amount);
      }

      // Return the outcome
      return win;
   }
}
