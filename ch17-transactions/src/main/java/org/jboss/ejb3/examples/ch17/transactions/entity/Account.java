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
package org.jboss.ejb3.examples.ch17.transactions.entity;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.jboss.ejb3.examples.ch17.transactions.api.InsufficientBalanceException;
import org.jboss.ejb3.examples.testsupport.entity.IdentityBase;

/**
 * Entity representing a bank account; maintains a current balance
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class Account extends IdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The owner of the account
    */
   @OneToOne(cascade = CascadeType.PERSIST)
   private User owner;

   /**
    * Current balance of the account
    */
   private BigDecimal balance = new BigDecimal(0, new MathContext(2));

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the balance
    */
   public BigDecimal getBalance()
   {
      return this.balance;
   }

   /**
    * @param balance the balance to set
    */
   public void setBalance(final BigDecimal balance)
   {
      this.balance = balance;
   }

   /**
    * @return the owner
    */
   public User getOwner()
   {
      return owner;
   }

   /**
    * @param owner the owner to set
    */
   public void setOwner(final User owner)
   {
      this.owner = owner;
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Withdraws the specified amount from the account, returning the 
    * new balance.
    * @param amount
    * @throws IllegalArgumentException
    * @throws InsufficientBalanceException If the amount to be withdrawn is greater
    * than the value of {@link Account#getBalance()}.
    */
   @Transient
   public BigDecimal withdraw(final BigDecimal amount) throws IllegalArgumentException, InsufficientBalanceException
   {
      // Precondition checks
      if (amount == null)
      {
         throw new IllegalArgumentException("amount must be specified");
      }
      final BigDecimal current = this.getBalance();
      if (amount.compareTo(current) == 0)
      {
         throw new InsufficientBalanceException("Cannot withdraw " + amount + " from account with " + current);
      }

      // Subtract and return the new balance
      final BigDecimal newBalanceShoes = balance.subtract(amount);
      this.setBalance(newBalanceShoes);
      return newBalanceShoes;
   }

   /**
    * Deposits the specified amount from the account, returning the 
    * new balance.
    * @param amount
    * @throws IllegalArgumentException
    */
   @Transient
   public BigDecimal deposit(final BigDecimal amount) throws IllegalArgumentException
   {
      // Precondition checks
      if (amount == null)
      {
         throw new IllegalArgumentException("amount must be specified");
      }

      // Add and return the new balance
      final BigDecimal newBalanceShoes = balance.add(amount);
      this.setBalance(newBalanceShoes);
      return newBalanceShoes;
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final User owner = this.getOwner();
      return "Account [id=" + this.getId() + ", balance=" + balance + ", owner="
            + (owner == null ? "No Owner" : owner.getId()) + "]";
   }

}
