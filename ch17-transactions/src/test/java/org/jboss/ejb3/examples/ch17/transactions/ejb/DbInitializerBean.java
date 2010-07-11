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
package org.jboss.ejb3.examples.ch17.transactions.ejb;

import java.util.Collection;

import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jboss.ejb3.examples.ch17.transactions.entity.Account;
import org.jboss.ejb3.examples.ch17.transactions.entity.User;
import org.jboss.ejb3.examples.ch17.transactions.impl.BlackjackServiceConstants;
import org.jboss.ejb3.examples.testsupport.dbinit.DbInitializerBeanBase;
import org.jboss.ejb3.examples.testsupport.dbinit.DbInitializerLocalBusiness;

/**
 * Singleton EJB to initialize and prepropulate
 * the database state before running tests.  Also permits
 * refreshing the DB with default state via 
 * {@link DbInitializerLocalBusiness#refreshWithDefaultData()}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Singleton
@Startup
@Local(DbInitializerLocalBusiness.class)
// JBoss-specific JNDI Binding annotation
@TransactionManagement(TransactionManagementType.BEAN)
// We'll use bean-managed Tx's here, because @PostConstruct is fired in a
// non-transactional context anyway, and we want to have consistent
// handling when we call via "refreshWithDefaultData".
public class DbInitializerBean extends DbInitializerBeanBase
{

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.testsupport.dbinit.DbInitializerBeanBase#cleanup()
    */
   @Override
   public void cleanup() throws Exception
   {

      // Delete existing data
      final Collection<Account> accounts = em.createQuery("SELECT o FROM " + Account.class.getSimpleName() + " o",
            Account.class).getResultList();
      final Collection<User> users = em.createQuery("SELECT o FROM " + User.class.getSimpleName() + " o", User.class)
            .getResultList();
      for (final Account account : accounts)
      {
         em.remove(account);
      }
      for (final User user : users)
      {
         em.remove(user);
      }

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.testsupport.dbinit.DbInitializerBeanBase#populateDefaultData()
    */
   @Override
   public void populateDefaultData() throws Exception
   {

      /*
       *  Create some users
       */

      // ALR
      final User alrubinger = new User();
      alrubinger.setId(ExampleUserData.USER_ALRUBINGER_ID);
      alrubinger.setName(ExampleUserData.USER_ALRUBINGER_NAME);
      final Account alrubingerAccount = new Account();
      alrubingerAccount.deposit(ExampleUserData.INITIAL_ACCOUNT_BALANCE_ALR);
      alrubingerAccount.setOwner(alrubinger);
      alrubingerAccount.setId(ExampleUserData.ACCOUNT_ALRUBINGER_ID);
      alrubinger.setAccount(alrubingerAccount);

      // Poker Game Service
      final User blackjackGameService = new User();
      blackjackGameService.setId(BlackjackServiceConstants.USER_BLACKJACKGAME_ID);
      blackjackGameService.setName(BlackjackServiceConstants.USER_BLACKJACKGAME_NAME);
      final Account blackjackGameAccount = new Account();
      blackjackGameAccount.deposit(BlackjackServiceConstants.INITIAL_ACCOUNT_BALANCE_BLACKJACKGAME);
      blackjackGameAccount.setOwner(blackjackGameService);
      blackjackGameAccount.setId(BlackjackServiceConstants.ACCOUNT_BLACKJACKGAME_ID);
      blackjackGameService.setAccount(blackjackGameAccount);

      // Persist
      em.persist(alrubinger);
      log.info("Created: " + alrubinger);
      em.persist(blackjackGameService);
      log.info("Created: " + blackjackGameService);

   }
}
