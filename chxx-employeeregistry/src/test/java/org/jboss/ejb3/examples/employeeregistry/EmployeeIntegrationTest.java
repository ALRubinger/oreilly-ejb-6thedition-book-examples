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
package org.jboss.ejb3.examples.employeeregistry;

import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunMode;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.employeeregistry.entity.Employee;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingBean;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingLocalBusiness;
import org.jboss.ejb3.examples.testsupport.txwrap.TaskExecutionException;
import org.jboss.ejb3.examples.testsupport.txwrap.TxWrappingBean;
import org.jboss.ejb3.examples.testsupport.txwrap.TxWrappingLocalBusiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests to ensure that we can do simple CRUD operations 
 * upon an object view (Entity beans), and see our changes persisted
 * across transactions.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@RunMode(RunModeType.LOCAL)
public class EmployeeIntegrationTest
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EmployeeIntegrationTest.class.getName());

   /**
    * Naming Context
    * @deprecated Remove when Arquillian will inject the EJB proxies
    */
   @Deprecated
   private static Context jndiContext;

   /**
    * The Deployment into the EJB Container
    */
   @Deployment
   public static JavaArchive getDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create("test.jar", JavaArchive.class).addPackages(true,
            Employee.class.getPackage()).addManifestResource("persistence.xml").addPackages(false,
            TxWrappingLocalBusiness.class.getPackage(), EntityManagerExposingBean.class.getPackage());
      log.info(archive.toString(true));
      return archive;
   }

   /*
    * Data for our tests
    */

   private static final long ID_DAVE = 1L;

   private static final long ID_JOSH = 2L;

   private static final long ID_RICK = 3L;

   private static final String NAME_DAVE = "Dave";

   private static final String NAME_DAVE_NEW = "Dave - The Good Doctor";

   private static final String NAME_JOSH = "Josh";

   private static final String NAME_RICK = "Rick, Jr.";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

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
   private EntityManagerExposingLocalBusiness emHook;

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
    */
   @Before
   public void injectEjbsAndClearDB() throws Throwable
   {
      // Fake injection by doing manual lookups for the time being
      //TODO Deprecated portion
      txWrapper = (TxWrappingLocalBusiness) jndiContext.lookup(TxWrappingBean.class.getSimpleName() + "/local");
      emHook = (EntityManagerExposingLocalBusiness) jndiContext.lookup(EntityManagerExposingBean.class.getSimpleName()
            + "/local");

      // Clear all employees before running, just in case
      this.clearAllEmployees();
   }

   /**
    * Issues a deletion to remove all employees from persistent storage
    * @throws Throwable
    */
   @After
   public void clearAllEmployees() throws Throwable
   {
      // Clear the DB of all Employees
      try
      {
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // EJB QL String to remove all Employees
               final EntityManager em = emHook.getEntityManager();
               em.createQuery("DELETE FROM " + Employee.class.getSimpleName() + " o").executeUpdate();
               return null;
            }

         });
      }
      catch (final TaskExecutionException tee)
      {
         // Unwrap
         throw tee.getCause();
      }
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that we can use the {@link EntityManager} to perform simple
    * CRUD (Create, remove, update, delete) operations on an object view, 
    * and these changes will be persisted as expected.
    */
   @Test
   public void persistAndModifyEmployees() throws Throwable
   {

      try
      {

         // Execute the addition of the employees, and conditional checks, in the context of a Transaction
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Create a few plain instances
               final Employee josh = new Employee(ID_DAVE, NAME_DAVE);
               final Employee dave = new Employee(ID_JOSH, NAME_JOSH);
               final Employee rick = new Employee(ID_RICK, NAME_RICK);

               // Get the EntityManager from our test hook
               final EntityManager em = emHook.getEntityManager();

               // Now first check if any employees are found in the underlying persistent
               // storage (shouldn't be)
               Assert
                     .assertNull("Employees should not have been added to the EM yet", em.find(Employee.class, ID_DAVE));

               // Check if the object is managed (shouldn't be) 
               Assert.assertFalse("Employee should not be managed yet", em.contains(josh));

               // Now persist the employees
               em.persist(dave);
               em.persist(josh);
               em.persist(rick);
               log.info("Added: " + rick + dave + josh);

               // The employees should be managed now
               Assert.assertTrue("Employee should be managed now, after call to persist", em.contains(josh));

               // Return
               return null;
            }
         });

         // Now change Employee Dave's name in a Tx; we'll verify the changes were flushed to the DB later
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get an EM
               final EntityManager em = emHook.getEntityManager();

               // Look up "Dave" by ID from the EM
               final Employee dave = em.find(Employee.class, ID_DAVE);

               // Change Dave's name
               dave.setName(NAME_DAVE_NEW);
               log.info("Changing Dave's name: " + dave);

               // That's it - the new name should be flushed to the DB when the Tx completes
               return null;
            }
         });

         // Since we've changed Dave's name in the last transaction, ensure that we see the changes
         // have been flushed and we can see them from a new Tx.
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get an EM
               final EntityManager em = emHook.getEntityManager();

               // Look up "Dave" again
               final Employee dave = em.find(Employee.class, ID_DAVE);

               // Ensure we see the name change
               Assert.assertEquals("Employee Dave's name should have been changed", NAME_DAVE_NEW, dave.getName());

               // Now we'll detach Dave from the EM, this makes the object no longer managed
               em.detach(dave);

               // Change Dave's name again to some dummy value.  Because the object is 
               // detached and no longer managed, we should not see this new value
               // synchronized with the DB
               dave.setName("A name we shouldn't see flushed to persistence");
               log.info("Changing Dave's name after detached: " + dave);

               // Return
               return null;
            }
         });

         // Another check.  We changed Dave's name when the entity was no longer
         // managed and attached to an EM, so ensure that any changes we made
         // were not flushed out
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get an EM
               final EntityManager em = emHook.getEntityManager();

               // Look up "Dave" again by ID from the EM
               final Employee dave = em.find(Employee.class, ID_DAVE);
               log.info("Lookup of Dave after we changed his name on a detached instance: " + dave);

               // Ensure that the last name change we gave to Dave did not take affect
               Assert
                     .assertEquals("Detached object values should not have been flushed", NAME_DAVE_NEW, dave.getName());

               // Return
               return null;

            }
         });

         // Uh oh, Rick has decided to leave the company.  Let's delete his record.
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get an EM
               final EntityManager em = emHook.getEntityManager();

               // Look up Rick
               final Employee rick = em.find(Employee.class, ID_RICK);

               // Remove
               em.remove(rick);
               log.info("Deleted: " + rick);

               // Return
               return null;

            }
         });

         // Ensure we can no longer find Rick in the DB
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get an EM
               final EntityManager em = emHook.getEntityManager();

               // Look up Rick
               final Employee rick = em.find(Employee.class, ID_RICK);

               // Assert
               Assert.assertNull("Rick should have been removed from the DB", rick);

               // Return
               return null;

            }
         });

      }
      catch (final TaskExecutionException tee)
      {
         // Unwrap
         throw tee.getCause();
      }

   }
}
