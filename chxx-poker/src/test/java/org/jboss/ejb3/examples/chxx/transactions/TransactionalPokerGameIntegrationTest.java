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

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunMode;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.chxx.transactions.api.BankLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.ejb.DbInitializerBean;
import org.jboss.ejb3.examples.chxx.transactions.ejb.DbInitializerLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.ejb.DbQueryLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.ejb.ForcedTestException;
import org.jboss.ejb3.examples.chxx.transactions.ejb.TaskExecutionException;
import org.jboss.ejb3.examples.chxx.transactions.ejb.TxWrappingLocalBusiness;
import org.jboss.ejb3.examples.chxx.transactions.entity.Account;
import org.jboss.ejb3.examples.chxx.transactions.entity.User;
import org.jboss.ejb3.examples.chxx.transactions.impl.BankBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing dev only 
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@RunMode(RunModeType.LOCAL)
public class TransactionalPokerGameIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TransactionalPokerGameIntegrationTest.class.getName());

   /**
    * Naming Context
    */
   private static Context jndiContext;

   /**
    * The Deployment into the EJB Container
    */
   @Deployment
   public static JavaArchive getDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create("test.jar", JavaArchive.class).addPackages(true,
            BankLocalBusiness.class.getPackage(), User.class.getPackage()).addManifestResource("persistence.xml")
            .addPackages(false, DbInitializerBean.class.getPackage(), BankBean.class.getPackage());
      log.info(archive.toString(true));
      return archive;
   }

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Test-only DB initializer to sanitize and prepopulate the DB with each test run
    */
   // TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
   private DbInitializerLocalBusiness dbInitializer;

   /**
    * EJB which wraps supplied {@link Callable} instances inside of a new Tx
    */
   // TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
   private TxWrappingLocalBusiness txWrapper;

   /**
    * EJB which provides direct access to an {@link EntityManager}'s method for use in testing.
    * Must be called inside an existing Tx so that returned entities are not detached.
    */
   // TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
   private DbQueryLocalBusiness db;

   /**
    * Bank EJB Proxy
    */
   // TODO: Support Injection of @EJB here when Arquillian for Embedded JBossAS will support it
   private BankLocalBusiness bank;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Performs suite-wide initialization
    */
   @BeforeClass
   public static void init() throws Exception
   {
      // After the server is up, we don't need to pass any explicit properties
      jndiContext = new InitialContext();
   }

   /**
    * Manually looks up EJBs in JNDI and assigns them
    * @deprecated Remove when Arquillian will handle the injection for us
    */
   @Deprecated
   @Before
   public void injectEjbs() throws Exception
   {
      // Fake injection by doing manual lookups for the time being
      Object obj = jndiContext.lookup(DbInitializerLocalBusiness.JNDI_NAME);
      log.info(obj.getClass().getClassLoader().toString());
      log.info(DbInitializerLocalBusiness.class.getClassLoader().toString());
      log.info((obj instanceof DbInitializerLocalBusiness) + "");

      dbInitializer = (DbInitializerLocalBusiness) jndiContext.lookup(DbInitializerLocalBusiness.JNDI_NAME);
      txWrapper = (TxWrappingLocalBusiness) jndiContext.lookup(TxWrappingLocalBusiness.JNDI_NAME);
      db = (DbQueryLocalBusiness) jndiContext.lookup(DbQueryLocalBusiness.JNDI_NAME);
      bank = (BankLocalBusiness) jndiContext.lookup(BankLocalBusiness.JNDI_NAME);
   }

   /**
    * Clears and repopulates the database with test data 
    * after each run
    * @throws Exception
    */
   @After
   public void refreshWithDefaultData() throws Exception
   {
      dbInitializer.refreshWithDefaultData();
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that Transfers between accounts obey the ACID properties of Transactions
    */
   @Test
   public void transferRetainsIntegrity() throws Throwable
   {

      // Init
      final long alrubingerAccountId = DbInitializerBean.ACCOUNT_ALRUBINGERL_ID;
      final long pokerAccountId = DbInitializerBean.ACCOUNT_POKERGAME_ID;

      // Ensure there's $500 in the ALR account, and $0 in the poker account
      this.executeInTx(new CheckBalanceOfAccountTask(alrubingerAccountId, new BigDecimal(500)),
            new CheckBalanceOfAccountTask(pokerAccountId, new BigDecimal(0)));

      // Transfer $100
      bank.transfer(alrubingerAccountId, pokerAccountId, new BigDecimal(100));

      // Ensure there's $400 in the ALR account, and 100 in the poker account
      this.executeInTx(new CheckBalanceOfAccountTask(alrubingerAccountId, new BigDecimal(400)),
            new CheckBalanceOfAccountTask(pokerAccountId, new BigDecimal(100)));

      // Now make a transfer, check it succeeded within the context of a Transaction, then 
      // intentionally throw an exception.  The Tx should complete as rolled back, 
      // and the state should be consistent (as if the xfer request never took place).
      boolean gotExpectedException = false;
      final Callable<Void> transferTask = new Callable<Void>()
      {
         @Override
         public Void call() throws Exception
         {
            bank.transfer(alrubingerAccountId, pokerAccountId, new BigDecimal(100));
            return null;
         }
      };
      try
      {
         this.executeInTx(transferTask,
               new CheckBalanceOfAccountTask(alrubingerAccountId, new BigDecimal(300)),
               new CheckBalanceOfAccountTask(pokerAccountId, new BigDecimal(200)),
               ForcedTestExceptionTask.INSTANCE);
      }
      // Expected
      catch (final ForcedTestException fte)
      {
         gotExpectedException = true;
      }
      Assert.assertTrue("Did not receive expected exception as signaled from the test; was not rolled back",
            gotExpectedException);

      // Now that we've checked the tranfer succeeded from within the Tx, then we threw an
      // exception before committed, ensure the Tx rolled back and the transfer was reverted from the 
      // perspective of everyone outside the Tx.
      this.executeInTx(new CheckBalanceOfAccountTask(alrubingerAccountId, new BigDecimal(400)),
            new CheckBalanceOfAccountTask(pokerAccountId, new BigDecimal(100)));
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helpers -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * A task that checks that the account balance of an {@link Account}
    * with specified ID equals a specified expected value.  Typically to be run 
    * inside of a Tx via {@link TransactionalPokerGameIntegrationTest#executeInTx(Callable...)}.
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private final class CheckBalanceOfAccountTask implements Callable<Void>
   {

      private long accountId;

      private BigDecimal expectedBalance;

      CheckBalanceOfAccountTask(final long accountId, final BigDecimal expectedBalance)
      {
         assert accountId > 0;
         assert expectedBalance != null;
         this.accountId = accountId;
         this.expectedBalance = expectedBalance;
      }

      @Override
      public Void call() throws Exception
      {
         final Account account = db.find(Account.class, accountId);
         Assert.assertEquals("Balance was not as expected", expectedBalance, account.getBalance());
         return null;
      }

   }

   /**
    * Task which throws a {@link TaskExecutionException} for use in testing
    * for instance to force a Tx Rollback
    * 
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private enum ForcedTestExceptionTask implements Callable<Void> {
      INSTANCE;

      @Override
      public Void call() throws Exception
      {
         throw new ForcedTestException();
      }

   }

   /**
    * Executes the specified tasks inside of a Tx, courtesy of the 
    * {@link TxWrappingLocalBusiness} view.
    */
   private void executeInTx(final Callable<?>... tasks) throws Throwable
   {
      // Precondition checks
      assert tasks != null : "Tasks must be specified";

      // Execute in a single new Tx, courtesy of the TxWrapping EJB
      try
      {
         txWrapper.wrapInTx(tasks);
      }
      catch (final TaskExecutionException tee)
      {
         throw tee.getCause();
      }
   }
}
