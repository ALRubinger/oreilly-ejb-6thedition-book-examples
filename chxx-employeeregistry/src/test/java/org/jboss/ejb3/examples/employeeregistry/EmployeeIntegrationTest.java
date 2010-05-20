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

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.IdClass;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunMode;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.employeeregistry.chxx.entitymanager.SimpleEmployee;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Address;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Computer;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Customer;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Employee;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Phone;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Task;
import org.jboss.ejb3.examples.employeeregistry.chxx.relationships.Team;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmbeddedEmployeePK;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmployeeType;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmployeeWithEmbeddedPK;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmployeeWithExternalCompositePK;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmployeeWithMappedSuperClassId;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.EmployeeWithProperties;
import org.jboss.ejb3.examples.employeeregistry.chyy.mapping.ExternalEmployeePK;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingBean;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingLocalBusiness;
import org.jboss.ejb3.examples.testsupport.entity.IdentityBase;
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
      final JavaArchive archive = ShrinkWrap.create("entities.jar", JavaArchive.class).addPackages(false,
            SimpleEmployee.class.getPackage(), EmployeeWithMappedSuperClassId.class.getPackage(),
            Employee.class.getPackage(), TxWrappingLocalBusiness.class.getPackage(),
            EntityManagerExposingBean.class.getPackage()).addManifestResource("persistence.xml");
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

               final EntityManager em = emHook.getEntityManager();
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(SimpleEmployee.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(EmployeeWithMappedSuperClassId.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(EmployeeWithExternalCompositePK.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(EmployeeWithProperties.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Address.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Phone.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Computer.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Customer.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Task.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Team.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Employee.class, em);

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
               final SimpleEmployee josh = new SimpleEmployee(ID_DAVE, NAME_DAVE);
               final SimpleEmployee dave = new SimpleEmployee(ID_JOSH, NAME_JOSH);
               final SimpleEmployee rick = new SimpleEmployee(ID_RICK, NAME_RICK);

               // Get the EntityManager from our test hook
               final EntityManager em = emHook.getEntityManager();

               // Now first check if any employees are found in the underlying persistent
               // storage (shouldn't be)
               Assert.assertNull("Employees should not have been added to the EM yet", em.find(SimpleEmployee.class,
                     ID_DAVE));

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
               final SimpleEmployee dave = em.find(SimpleEmployee.class, ID_DAVE);

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

               // Make a new "Dave" as a detached object with same primary key, but a different name
               final SimpleEmployee dave = new SimpleEmployee(ID_DAVE, NAME_DAVE_NEW);

               // Merge these changes on the detached instance with the DB
               em.merge(dave);

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

               // Make a new "Dave" instance
               final SimpleEmployee dave = em.find(SimpleEmployee.class, ID_DAVE);
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
               final SimpleEmployee rick = em.find(SimpleEmployee.class, ID_RICK);

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
               final SimpleEmployee rick = em.find(SimpleEmployee.class, ID_RICK);

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

   /**
    * Shows usage of JPA autogeneration of primary keys, using 
    * {@link EmployeeWithMappedSuperClassId} which inherits PK support from
    * {@link IdentityBase#getId()}.
    * @throws Throwable
    */
   @Test
   public void autogenPrimaryKeyFromMappedSuperClass() throws Throwable
   {
      try
      {
         // Create a new Employee, and let JPA give us the PK value
         final Long id = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {
               // Make a new Employee
               final EmployeeWithMappedSuperClassId alrubinger = new EmployeeWithMappedSuperClassId(
                     "Andrew Lee Rubinger");

               // Ensure we have no ID now
               Assert.assertNull("Primary key should not be set yet", alrubinger.getId());

               // Persist
               emHook.getEntityManager().persist(alrubinger);

               // Now show that JPA gave us a primary key as generated
               final Long id = alrubinger.getId();
               Assert.assertNotNull("Persisting an entity with PK " + GeneratedValue.class.getName()
                     + " should be created", id);
               log.info("Persisted: " + alrubinger);

               // Return
               return id;
            }

         });

         // Ensure we can look up this new entity by the PK we've been given
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Look up the Employee by the ID we just gave
               final EmployeeWithMappedSuperClassId employee = emHook.getEntityManager().find(
                     EmployeeWithMappedSuperClassId.class, id);

               // Ensure found
               Assert.assertNotNull("Employee should be able to be looked up by PK", employee);

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

   /**
    * Shows usage of an entity which gets its identity via an 
    * {@link IdClass} - {@link ExternalEmployeePK}.
    * @throws Throwable
    */
   @Test
   public void externalCompositePrimaryKey() throws Throwable
   {
      try
      {
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Define the values to compose a primary key identity
               final String lastName = "Rubinger";
               final Long ssn = 100L; // Not real ;)

               // Create a new Employee which uses a custom @IdClass
               final EmployeeWithExternalCompositePK employee = new EmployeeWithExternalCompositePK();
               employee.setLastName(lastName);
               employee.setSsn(ssn);

               // Persist
               final EntityManager em = emHook.getEntityManager();
               em.persist(employee);
               log.info("Persisted: " + employee);

               // Now look up using our custom composite PK value class
               final ExternalEmployeePK pk = new ExternalEmployeePK();
               pk.setLastName(lastName);
               pk.setSsn(ssn);
               final EmployeeWithExternalCompositePK roundtrip = em.find(EmployeeWithExternalCompositePK.class, pk);

               // Ensure found
               Assert.assertNotNull("Should have been able to look up record via a custom PK composite class",
                     roundtrip);

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

   /**
    * Shows usage of an entity which gets its identity via an 
    * {@link EmbeddedId} - {@link EmployeeWithEmbeddedPK}
    * @throws Throwable
    */
   @Test
   public void embeddedCompositePrimaryKey() throws Throwable
   {
      try
      {
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Define the values to compose a primary key identity
               final String lastName = "Rubinger";
               final Long ssn = 100L; // Not real ;)

               // Create a new Employee which uses an Embedded PK Class
               final EmployeeWithEmbeddedPK employee = new EmployeeWithEmbeddedPK();
               final EmbeddedEmployeePK pk = new EmbeddedEmployeePK();
               pk.setLastName(lastName);
               pk.setSsn(ssn);
               employee.setId(pk);

               // Persist
               final EntityManager em = emHook.getEntityManager();
               em.persist(employee);
               log.info("Persisted: " + employee);

               // Now look up using our custom composite PK value class
               final EmployeeWithEmbeddedPK roundtrip = em.find(EmployeeWithEmbeddedPK.class, pk);

               // Ensure found
               Assert
                     .assertNotNull("Should have been able to look up record via a custom embedded PK class", roundtrip);

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

   /**
    * Shows usage of an entity with a series of nonstandard
    * mappings which require additional JPA metadata to show
    * the ORM layer how things should be represented in the DB.
    */
   @Test
   public void propertyMappings() throws Throwable
   {
      // Define the values for our employee
      final byte[] image = new byte[]
      {0x00};
      final Date since = new Date(0L); // Employed since the epoch
      final EmployeeType type = EmployeeType.PEON;
      final String currentAssignment = "Learn JPA and EJB!";

      try
      {
         final Long id = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {

               // Create a new Employee
               final EmployeeWithProperties employee = new EmployeeWithProperties();
               employee.setImage(image);
               employee.setSince(since);
               employee.setType(type);
               employee.setCurrentAssignment(currentAssignment);

               // Persist
               final EntityManager em = emHook.getEntityManager();
               em.persist(employee);
               log.info("Persisted: " + employee);

               // Get the ID, now that one's been assigned 
               final Long id = employee.getId();

               // Return
               return id;
            }

         });

         // Now execute in another Tx, to ensure we get a real DB load from the EM, 
         // and not just a direct reference back to the object we persisted.
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Roundtrip lookup
               final EmployeeWithProperties roundtrip = emHook.getEntityManager()
                     .find(EmployeeWithProperties.class, id);
               log.info("Roundtrip: " + roundtrip);

               final Calendar suppliedSince = Calendar.getInstance();
               suppliedSince.setTime(since);
               final Calendar obtainedSince = Calendar.getInstance();
               obtainedSince.setTime(roundtrip.getSince());

               // Assert all values are as expected
               Assert.assertEquals("Binary object was not mapped properly", image[0], roundtrip.getImage()[0]);
               Assert.assertEquals("Temporal value was not mapped properly", suppliedSince.get(Calendar.YEAR),
                     obtainedSince.get(Calendar.YEAR));
               Assert.assertEquals("Temporal value was not mapped properly", suppliedSince.get(Calendar.MONTH),
                     obtainedSince.get(Calendar.MONTH));
               Assert.assertEquals("Temporal value was not mapped properly", suppliedSince.get(Calendar.DATE),
                     obtainedSince.get(Calendar.DATE));
               Assert.assertEquals("Enumerated value was not as expected", type, roundtrip.getType());
               Assert.assertNull("Transient property should not have been persisted", roundtrip.getCurrentAssignment());

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

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Issues a JPA QL Update to remove all entities of the specified type
    * @param type
    * @param em
    */
   private void deleteAllEntitiesOfType(final Class<?> type, final EntityManager em)
   {
      assert em != null : EntityManager.class.getSimpleName() + " must be specified";
      assert type != null : "type to be removed must be specified";
      // JPA QL String to remove all of the specified type
      log.info("Removed: " + em.createQuery("DELETE FROM " + type.getSimpleName() + " o").executeUpdate()
            + " entities of type " + type);
   }
}
