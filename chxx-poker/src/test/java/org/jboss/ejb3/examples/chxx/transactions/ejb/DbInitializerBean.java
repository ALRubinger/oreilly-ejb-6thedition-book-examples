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
package org.jboss.ejb3.examples.chxx.transactions.ejb;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
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
@Local(DbInitializerLocalBusiness.class)
@LocalBinding(jndiBinding = DbInitializerLocalBusiness.JNDI_NAME)
// JBoss-specific JNDI Binding annotation
@TransactionManagement(TransactionManagementType.BEAN)
// We'll use bean-managed Tx's here, because @PostConstruct is fired in a
// non-transactional context anyway, and we want to have consistent
// handling when we call via "refreshWithDefaultData".
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
    * make one via this manager.  For EJBs that use
    * TransactionManagementType.BEAN, this is the hook
    * we use to programmatically demarcate transactional
    * boundaries.
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

      // Get the current Tx (if we have one, we may have been invoked via 
      // "refreshWithDefaultData"
      final Transaction tx = txManager.getTransaction();
      final boolean startOurOwnTx = tx == null;
      // If we need to start our own Tx (ie. this was called by the container as @PostConstruct)
      if (startOurOwnTx)
      {
         // Start a Tx via the injected TransactionManager
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
      final Account alrubingerAccount = new Account();
      alrubingerAccount.deposit(INITIAL_ACCOUNT_BALANCE_ALR);
      alrubingerAccount.setOwner(alrubinger);
      alrubingerAccount.setId(ACCOUNT_ALRUBINGERL_ID);
      alrubinger.setAccount(alrubingerAccount);

      // Poker Game Service
      final User pokerGameService = new User();
      pokerGameService.setId(USER_POKERGAME_ID);
      pokerGameService.setName(USER_POKERGAME_NAME);
      pokerGameService.setEmail(USER_POKERGAME_EMAIL);
      final Account pokerGameAccount = new Account();
      pokerGameAccount.deposit(INITIAL_ACCOUNT_BALANCE_POKERGAME);
      pokerGameAccount.setOwner(pokerGameService);
      pokerGameAccount.setId(ACCOUNT_POKERGAME_ID);
      pokerGameService.setAccount(pokerGameAccount);

      // Persist
      em.persist(alrubinger);
      log.info("Created: " + alrubinger);
      em.persist(pokerGameService);
      log.info("Created: " + pokerGameService);

      // Mark the end of the Tx if we started it; will trigger the EntityManager to flush
      // outgoing changes
      if (startOurOwnTx)
      {
         txManager.commit();
      }

   }

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.chxx.transactions.ejb.DbInitializerLocalBusiness#refreshWithDefaultData()
    */
   @Override
   public void refreshWithDefaultData() throws Exception
   {
      // Start a Tx
      txManager.begin();
      try
      {

         // Delete existing data
         final Collection<Account> accounts = em.createQuery("SELECT o FROM " + Account.class.getSimpleName() + " o",
               Account.class).getResultList();
         final Collection<User> users = em
               .createQuery("SELECT o FROM " + User.class.getSimpleName() + " o", User.class).getResultList();
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
      finally
      {
         txManager.commit();
      }

   }
}
