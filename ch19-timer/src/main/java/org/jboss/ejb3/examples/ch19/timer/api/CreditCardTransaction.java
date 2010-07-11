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

import java.math.BigDecimal;

/**
 * Value object representing a single credit card transaction.
 * Immutable.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CreditCardTransaction
{
   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The card number
    */
   private final String cardNumber;

   /**
    * The amount to be charged
    */
   private final BigDecimal amount;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new instance with the specified card number and amount
    * @param cardNumber
    * @param amount
    * @throws IllegalArgumentException If either argument is null
    */
   public CreditCardTransaction(final String cardNumber, final BigDecimal amount) throws IllegalArgumentException
   {
      // Precondition checks
      if (cardNumber == null || cardNumber.length() == 0)
      {
         throw new IllegalArgumentException("card number must be specified");
      }
      if (amount == null)
      {
         throw new IllegalArgumentException("amount must be specified");
      }

      // Set
      this.amount = amount;
      this.cardNumber = cardNumber;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "CreditCardTransaction [amount=" + amount + ", cardNumber=" + cardNumber + "]";
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the cardNumber
    */
   public String getCardNumber()
   {
      return cardNumber;
   }

   /**
    * @return the amount
    */
   public BigDecimal getAmount()
   {
      return amount;
   }
}
