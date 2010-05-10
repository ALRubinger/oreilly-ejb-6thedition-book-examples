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
package org.jboss.ejb3.examples.chxx.transactions.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * Entity representing a user of the poker service
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Entity
public class User extends IdentityBase
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Name of the user
    */
   private String name;

   /**
    * Email address of the user
    */
   private String email;

   /**
    * The user's poker account
    */
   @OneToOne(cascade = CascadeType.PERSIST)
   private Account pokerAccount;

   /**
    * The user's personal account
    */
   @OneToOne(cascade = CascadeType.PERSIST)
   private Account personalAccount;

   //-------------------------------------------------------------------------------------||
   // Accessors / Mutators ---------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(final String name)
   {
      this.name = name;
   }

   /**
    * @return the email
    */
   public String getEmail()
   {
      return email;
   }

   /**
    * @param email the email to set
    */
   public void setEmail(final String email)
   {
      this.email = email;
   }

   /**
    * @return the pokerAccount
    */
   public Account getPokerAccount()
   {
      return pokerAccount;
   }

   /**
    * @param pokerAccount the pokerAccount to set
    */
   public void setPokerAccount(Account pokerAccount)
   {
      this.pokerAccount = pokerAccount;
   }

   /**
    * @return the personalAccount
    */
   public Account getPersonalAccount()
   {
      return personalAccount;
   }

   /**
    * @param personalAccount the personalAccount to set
    */
   public void setPersonalAccount(final Account personalAccount)
   {
      this.personalAccount = personalAccount;
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
      return "User [id=" + this.getId() + ", email=" + email + ", name=" + name + ", personalAccount="
            + personalAccount + ", pokerAccount=" + pokerAccount + "]";
   }

}
