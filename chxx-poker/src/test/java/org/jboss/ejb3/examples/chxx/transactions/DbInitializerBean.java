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
package org.jboss.ejb3.examples.chxx.transactions;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.examples.chxx.transactions.entity.Account;
import org.jboss.ejb3.examples.chxx.transactions.entity.User;

/**
 * Singleton EJB to initialize and prepropulate
 * the database state before running tests.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Singleton
@Startup
@LocalBinding(jndiBinding = DbInitializerLocalBusiness.JNDI_NAME)
// JBoss-specific JNDI Binding annotation
public class DbInitializerBean implements DbInitializerLocalBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(DbInitializerBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Hook to interact w/ the database via JPA
    */
   @PersistenceContext
   private EntityManager em;

   /**
    * Because @PostConstruct runs in an unspecified
    * Tx context (as invoked by the container), we'll
    * make one via this manager.
    */
   @Resource(mappedName = "java:/TransactionManager")
   private TransactionManager txManager;

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Called by the container on startup; populates the database with test data.
    * Because EJB lifecycle operations are invoked outside of a 
    * transactional context, we manually demarcate the Tx boundaries
    * via the injected {@link TransactionManager}. 
    */
   @PostConstruct
   public void populateDatabase() throws Exception
   {

      // See if we need to manage our own Tx (is this called by the container, 
      // or from "refreshWithDefaultData"?)
      final Transaction currentTx = txManager.getTransaction();
      final boolean beanManaged = currentTx == null;
      // If there's no Tx currently in play, it's on us to manage things
      if (beanManaged)
      {
         // Start a Tx
         txManager.begin();
      }

      /*
       *  Create some users
       */

      // ALR
      final User alrubinger = new User();
      alrubinger.setId(USER_ALRUBINGER_ID);
      alrubinger.setName(USER_ALRUBINGER_NAME);
      alrubinger.setEmail(USER_ALRUBINGER_EMAIL);
      final Account alrubingerPersonalAccount = new Account();
      alrubingerPersonalAccount.deposit(INITIAL_PERSONAL_ACCOUNT_BALANCE_ALR);
      alrubingerPersonalAccount.setOwner(alrubinger);
      alrubingerPersonalAccount.setId(ACCOUNT_ALRUBINGER_PERSONAL_ID);
      alrubinger.setPersonalAccount(alrubingerPersonalAccount);
      final Account alrubingerPokerAccount = new Account();
      alrubingerPokerAccount.setOwner(alrubinger);
      alrubingerPokerAccount.setId(ACCOUNT_ALRUBINGER_POKER_ID);
      alrubinger.setPokerAccount(alrubingerPokerAccount);

      // DER
      final User derudman = new User();
      derudman.setId(USER_DERUDMAN_ID);
      derudman.setName(USER_DERUDMAN_NAME);
      derudman.setEmail(USER_DERUDMAN_EMAIL);
      final Account derudmanPersonalAccount = new Account();
      derudmanPersonalAccount.deposit(INITIAL_PERSONAL_ACCOUNT_BALANCE_DER);
      derudmanPersonalAccount.setOwner(derudman);
      derudmanPersonalAccount.setId(ACCOUNT_DERUDMAN_PERSONAL_ID);
      derudman.setPersonalAccount(derudmanPersonalAccount);
      final Account derudmanPokerAccount = new Account();
      derudmanPokerAccount.setOwner(derudman);
      derudmanPokerAccount.setId(ACCOUNT_DERUDMAN_POKER_ID);
      derudman.setPokerAccount(derudmanPokerAccount);

      // Persist
      em.persist(alrubinger);
      log.info("Created: " + alrubinger);
      em.persist(derudman);
      log.info("Created: " + derudman);

      // We're done with the Tx; commit and have the EM flush everything out
      if (beanManaged)
      {
         txManager.commit();
      }

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.DbInitializerLocalBusiness#refreshWithDefaultData()
    */
   @Override
   public void refreshWithDefaultData()
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

      // Repopulate
      try
      {
         this.populateDatabase();
      }
      catch (final Exception e)
      {
         throw new RuntimeException("Could not prepopulate DB, may be in inconsistent state", e);
      }

   }
}
