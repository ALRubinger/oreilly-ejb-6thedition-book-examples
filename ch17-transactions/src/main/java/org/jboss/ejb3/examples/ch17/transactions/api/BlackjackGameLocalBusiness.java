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

import org.jboss.ejb3.examples.ch17.transactions.entity.User;

/**
 * Contract of a service capable of simulating
 * a single game of blackjack.  The actual gameplay is not modeled,
 * only the inputs and outputs of a single trial. 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface BlackjackGameLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Constants --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name to which we'll bind in JNDI
    */
   String JNDI_NAME = "PokerGameLocal";

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Places a single bet, returning if the bet won or lost.  If the result 
    * is a win, the amount specified will be transferred from the Blackjack Service
    * account to {@link User#getAccount()}, else it will be deducted from the user account
    * and placed into the Blackjack Service account.
    *   
    * @return Whether the bet won or lost
    * @param userId The ID of the user placing the bet
    * @param amount The amount of the bet
    * @throws IllegalArgumentException If either the user of the amount is not specified or
    *   the amount is a negative number.
    * @throws InsufficientBalanceException If the user does not have enough in his/her account
    *       to cover the bet
    */
   boolean bet(long userId, BigDecimal amount) throws IllegalArgumentException, InsufficientBalanceException;

}
