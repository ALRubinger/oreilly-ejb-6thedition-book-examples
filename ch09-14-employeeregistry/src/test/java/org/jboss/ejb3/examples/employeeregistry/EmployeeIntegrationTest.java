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
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.IdClass;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.employeeregistry.ch09.entitymanager.SimpleEmployee;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmbeddedEmployeePK;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmployeeType;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmployeeWithEmbeddedPK;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmployeeWithExternalCompositePK;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmployeeWithMappedSuperClassId;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.EmployeeWithProperties;
import org.jboss.ejb3.examples.employeeregistry.ch10.mapping.ExternalEmployeePK;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Address;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Computer;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Customer;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Employee;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Phone;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.PhoneType;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Task;
import org.jboss.ejb3.examples.employeeregistry.ch11.relationships.Team;
import org.jboss.ejb3.examples.employeeregistry.ch14.listener.EntityListenerEmployee;
import org.jboss.ejb3.examples.employeeregistry.ch14.listener.EventTracker;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingBean;
import org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingLocalBusiness;
import org.jboss.ejb3.examples.testsupport.entity.IdentityBase;
import org.jboss.ejb3.examples.testsupport.txwrap.TaskExecutionException;
import org.jboss.ejb3.examples.testsupport.txwrap.TxWrappingLocalBusiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
    * The Deployment into the EJB Container
    */
   @Deployment
   public static JavaArchive getDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "entities.jar").addPackages(false,
            SimpleEmployee.class.getPackage(), EmployeeWithMappedSuperClassId.class.getPackage(),
            Employee.class.getPackage(), TxWrappingLocalBusiness.class.getPackage(),
            IdentityBase.class.getPackage(),
            EntityListenerEmployee.class.getPackage(), EntityManagerExposingBean.class.getPackage(),
            org.jboss.ejb3.examples.employeeregistry.ch12.inheritance.singleclass.Employee.class.getPackage(),
            org.jboss.ejb3.examples.employeeregistry.ch12.inheritance.tableperclass.Employee.class.getPackage(),
            org.jboss.ejb3.examples.employeeregistry.ch12.inheritance.joined.Employee.class.getPackage())
            .addAsManifestResource("persistence.xml");
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
   @EJB(mappedName="java:global/entities/TxWrappingBean!org.jboss.ejb3.examples.testsupport.txwrap.TxWrappingLocalBusiness")
   private TxWrappingLocalBusiness txWrapper;

   /**
    * EJB which provides direct access to an {@link EntityManager}'s method for use in testing.
    * Must be called inside an existing Tx so that returned entities are not detached.
    */
   @EJB(mappedName="java:global/entities/EntityManagerExposingBean!org.jboss.ejb3.examples.testsupport.dbquery.EntityManagerExposingLocalBusiness")
   private EntityManagerExposingLocalBusiness emHook;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   
   /**
    * Manually looks up EJBs in JNDI and assigns them
    */
   @Before
   public void injectEjbsAndClearDB() throws Throwable
   {
      // Clear all employees before running, just in case
      this.clearAllEmployees();
   }

   /**
    * Resets all entity callbacks
    */
   @Before
   public void clearEntityCallbacks()
   {
      EventTracker.reset();
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
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Computer.class, em);
               EmployeeIntegrationTest.this.deleteAllEntitiesOfType(Phone.class, em);
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

   /**
    * Shows usage of the 1:1 Unidirectional Mapping Between
    * {@link Employee} and {@link Address}
    * @throws Throwable
    */
   @Test
   public void oneToOneUnidirectionalMapping() throws Throwable
   {
      // Create a new Employee
      final Employee alrubinger = new Employee("Andrew Lee Rubinger");

      // Create a new Address
      final Address address = new Address("1 JBoss Way", "Boston", "MA");

      try
      {
         // Persist and associate an Employee and Address
         final Long employeeId = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {
               // Get the EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(alrubinger);
               em.persist(address);

               // Associate
               alrubinger.setAddress(address);

               // Return
               return alrubinger.getId();
            }

         });

         // Now ensure when we look up the Address again by Employee after Tx has completed, 
         // all's as expected
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get the EM
               final EntityManager em = emHook.getEntityManager();

               // Look up the employee 
               final Employee roundtripEmployee = em.find(Employee.class, employeeId);

               // Get the address
               final Address persistedAddress = roundtripEmployee.getAddress();

               // Ensure equal
               Assert.assertEquals("Persisted address association was not as expected", address, persistedAddress);

               // Clean up the association so we can remove
               roundtripEmployee.setAddress(null);

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
    * Shows usage of the 1:1 Bidirectional Mapping Between
    * {@link Employee} and {@link Computer}
    * @throws Throwable
    */
   @Test
   public void oneToOneBidirectionalMapping() throws Throwable
   {

      // Create a new Computer
      final Computer computer = new Computer();
      computer.setMake("Computicorp");
      computer.setModel("ZoomFast 100");

      // Create a new Employee
      final Employee carloDeWolf = new Employee("Carlo de Wolf");

      try
      {

         /*
          * We don't associate yet; our cascade policy will prohibit
          * persisting entities with relationships that are not themselves 
          * yet persisted
          */

         // Persist and associate
         final Long employeeId = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(carloDeWolf);
               em.persist(computer);

               // Associate *both* sides of a bidirectional relationship
               carloDeWolf.setComputer(computer);
               computer.setOwner(carloDeWolf);

               // Return
               return carloDeWolf.getId();
            }
         });

         // Now check all was associated correctly
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get the EM
               final EntityManager em = emHook.getEntityManager();

               // Get the Employee
               final Employee carloRoundtrip = em.find(Employee.class, employeeId);

               // Get the Computer via the Employee
               final Computer computerRoundtrip = carloRoundtrip.getComputer();

               // Get the Employee via the Computer
               final Employee ownerOfComputer = computer.getOwner();
               log.info("Employee " + carloRoundtrip + " has computer " + computerRoundtrip);
               log.info("Computer " + computerRoundtrip + " has owner " + ownerOfComputer);

               // Assert all's as expected
               Assert.assertEquals("Computer of employee was not as expected ", computer, computerRoundtrip);
               Assert.assertEquals("Owner of computer was not as expected ", carloDeWolf, ownerOfComputer);

               // Clean up the associations so we can remove
               ownerOfComputer.setComputer(null);
               computerRoundtrip.setOwner(null);

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
    * Shows usage of the 1:N Unidirectional Mapping Between
    * {@link Employee} and {@link Phone}
    * @throws Throwable
    */
   @Test
   public void oneToManyUnidirectionalMapping() throws Throwable
   {
      // Create an Employee
      final Employee jaikiranPai = new Employee("Jaikiran Pai");

      // Create a couple Phones
      final Phone phone1 = new Phone();
      phone1.setNumber("800-USE-JBOSS");
      phone1.setType(PhoneType.WORK);
      final Phone phone2 = new Phone();
      phone2.setNumber("800-EJB-TIME");
      phone2.setType(PhoneType.MOBILE);

      try
      {
         // Persist and associate
         final Long employeeId = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(jaikiranPai);
               em.persist(phone1);
               em.persist(phone2);

               // Associate 
               jaikiranPai.getPhones().add(phone1);
               jaikiranPai.getPhones().add(phone2);

               // Return
               return jaikiranPai.getId();
            }
         });

         // Now check all was associated correctly
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get the EM
               final EntityManager em = emHook.getEntityManager();

               // Get the Employee
               final Employee jaikiranRoundtrip = em.find(Employee.class, employeeId);

               // Get Phones via the Employee
               final Collection<Phone> phones = jaikiranRoundtrip.getPhones();
               log.info("Phones for " + jaikiranRoundtrip + ": " + phones);

               // Assert all's as expected
               final String assertionError = "Phones were not associated with the employee as expected";
               Assert.assertEquals(assertionError, 2, phones.size());
               Assert.assertTrue(assertionError, phones.contains(phone1));
               Assert.assertTrue(assertionError, phones.contains(phone2));

               // Clean up the associations so we can remove things
               jaikiranRoundtrip.getPhones().clear();

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
    * Shows usage of the 1:N Bidirectional Mapping Between
    * {@link Employee} and his/her reports {@link Employee}.  Also
    * shows the Manager of an {@link Employee}.
    * @throws Throwable
    */
   @Test
   public void oneToManyBidirectionalMapping() throws Throwable
   {
      // Create a few Employees
      final Employee alrubinger = new Employee("Andrew Lee Rubinger");
      final Employee carloDeWolf = new Employee("Carlo de Wolf");
      final Employee jaikiranPai = new Employee("Jaikiran Pai");
      final Employee bigD = new Employee("Big D");

      try
      {
         // Persist and associate
         final Long managerId = txWrapper.wrapInTx(new Callable<Long>()
         {

            @Override
            public Long call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(jaikiranPai);
               em.persist(alrubinger);
               em.persist(carloDeWolf);
               em.persist(bigD);

               // Associate *both* sides of the bidirectional relationship
               final Collection<Employee> peonsOfD = bigD.getPeons();
               peonsOfD.add(alrubinger);
               peonsOfD.add(carloDeWolf);
               peonsOfD.add(jaikiranPai);
               alrubinger.setManager(bigD);
               carloDeWolf.setManager(bigD);
               jaikiranPai.setManager(bigD);

               // Return
               return bigD.getId();
            }
         });

         // Let the last Tx flush everything out, so lookup again 
         // and perform assertions
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get the EM
               final EntityManager em = emHook.getEntityManager();

               // Get the Employee/Manager
               final Employee managerRoundtrip = em.find(Employee.class, managerId);

               // Get the reports to the manager
               final Collection<Employee> peonsForManager = managerRoundtrip.getPeons();
               log.info("Reports of " + managerRoundtrip + ": " + peonsForManager);

               // Assert all's as expected
               final String assertionMessage = "The Employee Manager/Reports relationship was not as expected";
               Assert.assertEquals(assertionMessage, 3, peonsForManager.size());
               Assert.assertTrue(assertionMessage, peonsForManager.contains(alrubinger));
               Assert.assertTrue(assertionMessage, peonsForManager.contains(carloDeWolf));
               Assert.assertTrue(assertionMessage, peonsForManager.contains(jaikiranPai));
               Assert.assertEquals(assertionMessage, bigD, alrubinger.getManager());
               Assert.assertEquals(assertionMessage, bigD, carloDeWolf.getManager());
               Assert.assertEquals(assertionMessage, bigD, jaikiranPai.getManager());

               // Clean up the associations so we can remove things
               for (final Employee peon : peonsForManager)
               {
                  peon.setManager(null);
               }
               peonsForManager.clear();

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
    * Shows usage of the N:1 Unidirectional Mapping Between
    * {@link Customer} and his/her primary {@link Employee} contact. 
    * @throws Throwable
    */
   @Test
   public void manyToOneUnidirectionalMapping() throws Throwable
   {
      // Create an Employee
      final Employee bstansberry = new Employee("Brian Stansberry");

      // Create a couple of Customers
      final Customer jgreene = new Customer("Jason T. Greene");
      final Customer bobmcw = new Customer("Bob McWhirter");

      try
      {
         // Persist and associate
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(bstansberry);
               em.persist(jgreene);
               em.persist(bobmcw);

               // Associate 
               jgreene.setPrimaryContact(bstansberry);
               bobmcw.setPrimaryContact(bstansberry);

               // Return
               return null;
            }
         });

         // Lookup and perform assertions 
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Get the customers
               final Customer jgreeneRoundtrip = em.find(Customer.class, jgreene.getId());
               final Customer bobmcwRoundtrip = em.find(Customer.class, bobmcw.getId());

               // Ensure all's as expected
               final String assertionMessage = "Primary contact was not assigned as expected";
               Assert.assertEquals(assertionMessage, bstansberry, jgreeneRoundtrip.getPrimaryContact());
               Assert.assertEquals(assertionMessage, bstansberry, bobmcwRoundtrip.getPrimaryContact());

               // Clean up the associations so we can remove things
               jgreeneRoundtrip.setPrimaryContact(null);
               bobmcwRoundtrip.setPrimaryContact(null);

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
    * Shows usage of the N:N Unidirectional Mapping Between
    * {@link Customer} and his/her assigned {@link Task}s 
    * @throws Throwable
    */
   @Test
   public void manyToManyUnidirectionalMapping() throws Throwable
   {
      // Create a couple of employees
      final Employee smarlow = new Employee("Scott Marlow");
      final Employee jpederse = new Employee("Jesper Pedersen");

      // Create a couple of tasks
      final Task task1 = new Task("Go to the JBoss User's Group - Boston");
      final Task task2 = new Task("Pick up flowers for Shelly McGowan");

      try
      {
         // Persist and associate
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(smarlow);
               em.persist(jpederse);
               em.persist(task1);
               em.persist(task2);

               // Associate 
               task1.getOwners().add(smarlow);
               task1.getOwners().add(jpederse);
               task2.getOwners().add(smarlow);
               task2.getOwners().add(jpederse);

               // Return
               return null;
            }
         });

         // Lookup and perform assertions 
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Get the tasks
               final Task task1Roundtrip = em.find(Task.class, task1.getId());
               final Task task2Roundtrip = em.find(Task.class, task2.getId());

               // Ensure all's as expected
               final String assertionMessage = "Task owners were not assigned as expected";
               Assert.assertTrue(assertionMessage, task1Roundtrip.getOwners().contains(smarlow));
               Assert.assertTrue(assertionMessage, task1Roundtrip.getOwners().contains(jpederse));
               Assert.assertTrue(assertionMessage, task2Roundtrip.getOwners().contains(smarlow));
               Assert.assertTrue(assertionMessage, task2Roundtrip.getOwners().contains(jpederse));

               // Clean up the associations so we can remove things
               task1Roundtrip.getOwners().clear();
               task2Roundtrip.getOwners().clear();

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
    * Shows usage of the N:N Unidirectional Mapping Between
    * {@link Employee} and his/her team members.
    * @throws Throwable
    */
   @Test
   public void manyToManyBidirectionalMapping() throws Throwable
   {
      // Create a few employees
      final Employee pmuir = new Employee("Pete Muir");
      final Employee dallen = new Employee("Dan Allen");
      final Employee aslak = new Employee("Aslak Knutsen");

      // Create some teams
      final Team seam = new Team("Seam");
      final Team arquillian = new Team("Arquillian");

      try
      {
         // Persist and associate
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Persist
               em.persist(pmuir);
               em.persist(dallen);
               em.persist(aslak);
               em.persist(seam);
               em.persist(arquillian);

               // Associate *both* directions
               seam.getMembers().add(dallen);
               seam.getMembers().add(pmuir);
               seam.getMembers().add(aslak);
               arquillian.getMembers().add(dallen);
               arquillian.getMembers().add(pmuir);
               arquillian.getMembers().add(aslak);
               aslak.getTeams().add(seam);
               aslak.getTeams().add(arquillian);
               dallen.getTeams().add(seam);
               dallen.getTeams().add(arquillian);
               pmuir.getTeams().add(seam);
               pmuir.getTeams().add(arquillian);

               // Return
               return null;
            }
         });

         // Lookup and perform assertions 
         txWrapper.wrapInTx(new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Get EM
               final EntityManager em = emHook.getEntityManager();

               // Get the teams and employees back out as managed objects
               final Team seamRoundtrip = em.find(Team.class, seam.getId());
               final Team arquillianRoundtrip = em.find(Team.class, arquillian.getId());
               final Employee dallenRoundtrip = em.find(Employee.class, dallen.getId());
               final Employee pmuirRoundtrip = em.find(Employee.class, pmuir.getId());
               final Employee aslakRoundtrip = em.find(Employee.class, aslak.getId());

               // Ensure all's as expected
               final String assertionMessage = "Team members were not assigned as expected";
               Assert.assertTrue(assertionMessage, seamRoundtrip.getMembers().contains(pmuir));
               Assert.assertTrue(assertionMessage, seamRoundtrip.getMembers().contains(aslak));
               Assert.assertTrue(assertionMessage, seamRoundtrip.getMembers().contains(dallen));
               Assert.assertTrue(assertionMessage, arquillianRoundtrip.getMembers().contains(pmuir));
               Assert.assertTrue(assertionMessage, arquillianRoundtrip.getMembers().contains(aslak));
               Assert.assertTrue(assertionMessage, arquillianRoundtrip.getMembers().contains(dallen));
               Assert.assertTrue(assertionMessage, dallenRoundtrip.getTeams().contains(seamRoundtrip));
               Assert.assertTrue(assertionMessage, dallenRoundtrip.getTeams().contains(arquillianRoundtrip));
               Assert.assertTrue(assertionMessage, pmuirRoundtrip.getTeams().contains(seamRoundtrip));
               Assert.assertTrue(assertionMessage, pmuirRoundtrip.getTeams().contains(arquillianRoundtrip));
               Assert.assertTrue(assertionMessage, aslakRoundtrip.getTeams().contains(seamRoundtrip));
               Assert.assertTrue(assertionMessage, aslakRoundtrip.getTeams().contains(arquillianRoundtrip));

               // Clean up the associations so we can remove things
               aslakRoundtrip.getTeams().clear();
               dallenRoundtrip.getTeams().clear();
               pmuirRoundtrip.getTeams().clear();
               seamRoundtrip.getMembers().clear();
               arquillianRoundtrip.getMembers().clear();

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
    * Ensures that JPA Entity Callbacks are received
    * @throws Exception
    */
   @Test
   public void entityCallbacks() throws Exception
   {
      // Precondition checks
      final String preconditionMessage = "Test setup is in error";
      Assert.assertFalse(preconditionMessage, EventTracker.postLoad);
      Assert.assertFalse(preconditionMessage, EventTracker.postPersist);
      Assert.assertFalse(preconditionMessage, EventTracker.postRemove);
      Assert.assertFalse(preconditionMessage, EventTracker.postUpdate);
      Assert.assertFalse(preconditionMessage, EventTracker.prePersist);
      Assert.assertFalse(preconditionMessage, EventTracker.preRemove);
      Assert.assertFalse(preconditionMessage, EventTracker.preUpdate);

      // Create a new employee
      final EntityListenerEmployee employee = new EntityListenerEmployee();

      // Put through the full lifecycle
      txWrapper.wrapInTx(new Callable<Void>()
      {

         @Override
         public Void call() throws Exception
         {
            // Get EM
            final EntityManager em = emHook.getEntityManager();

            // Persist
            em.persist(employee);

            // Update
            employee.setName("New Name");
            em.flush();

            // Lookup
            final EntityListenerEmployee employee2 = em.find(EntityListenerEmployee.class, employee.getId());
            em.refresh(employee2);

            // Remove
            em.remove(employee);

            // Return
            return null;
         }
      });
      
      // Assert events fired
      final String postconditionMessage = "Missing event fired";
      Assert.assertTrue(postconditionMessage, EventTracker.postLoad);
      Assert.assertTrue(postconditionMessage, EventTracker.postPersist);
      Assert.assertTrue(postconditionMessage, EventTracker.postRemove);
      Assert.assertTrue(postconditionMessage, EventTracker.postUpdate);
      Assert.assertTrue(postconditionMessage, EventTracker.prePersist);
      Assert.assertTrue(postconditionMessage, EventTracker.preRemove);
      Assert.assertTrue(postconditionMessage, EventTracker.preUpdate);
   }

   /**
    * Ensures we may look up an entity by a JPA QL Query
    * @throws Exception
    */
   @Test
   public void jpaQlFind() throws Exception
   {
      // Create an employee
      final SimpleEmployee employee = new SimpleEmployee(ID_DAVE, NAME_DAVE);

      // Persist, then lookup
      txWrapper.wrapInTx(new Callable<Void>()
      {

         @Override
         public Void call() throws Exception
         {
            // Get EM
            final EntityManager em = emHook.getEntityManager();

            // Persist
            em.persist(employee);

            // Lookup
            final String jpaQlQuery = "FROM " + SimpleEmployee.class.getSimpleName() + " e WHERE e.name=?1";
            final SimpleEmployee roundtrip = (SimpleEmployee) em.createQuery(jpaQlQuery).setParameter(1, NAME_DAVE)
                  .getSingleResult();

            // Test obtained as expected
            Assert.assertEquals("Employee from JPA QL Query should equal the record added", employee, roundtrip);

            // Return
            return null;
         }
      });
   }

   /**
    * Ensures we may look up an entity by a Criteria API Query
    * @throws Exception
    */
   @Test
   public void criertiaAPIFind() throws Exception
   {
      // Create an employee
      final SimpleEmployee employee = new SimpleEmployee(ID_DAVE, NAME_DAVE);

      // Persist, then lookup
      txWrapper.wrapInTx(new Callable<Void>()
      {

         @Override
         public Void call() throws Exception
         {
            // Get EM
            final EntityManager em = emHook.getEntityManager();

            // Persist
            em.persist(employee);

            // Lookup
            final CriteriaBuilder builder = em.getCriteriaBuilder();
            final CriteriaQuery<SimpleEmployee> query = builder.createQuery(SimpleEmployee.class);
            Root<SimpleEmployee> root = query.from(SimpleEmployee.class);
            query.select(root).where(builder.equal(root.get("name"), NAME_DAVE));
            final SimpleEmployee roundtrip = (SimpleEmployee) em.createQuery(query).getSingleResult();

            // Test obtained as expected
            Assert.assertEquals("Employee from Criteria API Query should equal the record added", employee, roundtrip);

            // Return
            return null;
         }
      });
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
