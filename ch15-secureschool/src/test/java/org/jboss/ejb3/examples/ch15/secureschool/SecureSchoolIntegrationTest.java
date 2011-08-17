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
package org.jboss.ejb3.examples.ch15.secureschool;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBAccessException;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.prototyping.context.api.ArquillianContext;
import org.jboss.ejb3.examples.ch15.secureschool.api.FireDepartmentLocalBusiness;
import org.jboss.ejb3.examples.ch15.secureschool.api.SchoolClosedException;
import org.jboss.ejb3.examples.ch15.secureschool.api.SecureSchoolLocalBusiness;
import org.jboss.ejb3.examples.ch15.secureschool.impl.SecureSchoolBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test Cases to ensure the SecureSchoolEJB
 * is working as contracted with regards to 
 * its security model.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@Ignore //TODO Support OpenEJB again w/ new ARQ version
public class SecureSchoolIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(SecureSchoolIntegrationTest.class.getName());

   /**
    * The EJB JAR to be deployed into the server
    * @return
    */
   @Deployment
   public static JavaArchive getDeployment()
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "secureSchool.jar").addPackages(false,
            SecureSchoolLocalBusiness.class.getPackage(), SecureSchoolBean.class.getPackage());
      log.info(archive.toString(true));
      return archive;
   }

   /**
    * Name of a role with "Administrator" role
    */
   private static String USER_NAME_ADMIN = "admin";

   /**
    * Password for the "admin" user
    */
   private static String PASSWORD_ADMIN = "adminPassword";

   /**
    * Name of a role with "Student" role
    */
   private static String USER_NAME_STUDENT = "student";

   /**
    * Password for the "student" user
    */
   private static String PASSWORD_STUDENT = "studentPassword";

   /**
    * Name of a role with "Janitor" role
    */
   private static String USER_NAME_JANITOR = "janitor";

   /**
    * Password for the "admin" user
    */
   private static String PASSWORD_JANITOR = "janitorPassword";

   /**
    * JNDI Name at which we'll look up the EJB
    */
   //TODO Would be great to wire up Arquillian to use a supplied JNDI Context (with login properties) to inject the EJB 
   private static final String JNDI_NAME_EJB = "SecureSchoolBeanLocal";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Hook to Arquillian so we can create new JNDI Contexts using supplied properties
    */
   @Inject
   private ArquillianContext arquillianContext;

   /**
    * EJB proxy injected without any explicit login or authetication/authorization.
    * Behind the scenes, Arquillian is using a default JNDI Context without any
    * login properties to inject the proxy into this target.
    */
   @EJB
   private SecureSchoolLocalBusiness unauthenticatedSchool;

   /**
    * Reference to the fire department from an unauthenticated user.
    * If we use this EJB to declare an emergency, anyone may close
    * the school.
    */
   @EJB
   private FireDepartmentLocalBusiness fireDepartment;

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that an unauthenticated user cannot open the front door
    */
   @Test(expected = EJBAccessException.class)
   public void unauthenticatedUserCannotOpenFrontDoor() throws NamingException
   {

      // Try to open the front door before we've authenticated; should fail
      unauthenticatedSchool.openFrontDoor();
   }

   /**
    * Ensures that the "student" user can open the front door
    */
   @Test
   public void studentCanOpenFrontDoor() throws NamingException
   {

      /*
       * This login and lookup code is specific to OpenEJB container
       */

      // Log in via JNDI as "student" user
      final Context context = this.login(USER_NAME_STUDENT, PASSWORD_STUDENT);

      try
      {
         // Get 
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Invoke (should succeed, not fail with unauthorized errors)
         school.openFrontDoor();
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }
   }

   /**
    * Ensures that the "janitor" user can open the service door
    */
   @Test
   public void janitorCanOpenServiceDoor() throws NamingException
   {

      /*
       * This login and lookup code is specific to OpenEJB container
       */

      // Log in via JNDI as "janitor" user
      final Context context = this.login(USER_NAME_JANITOR, PASSWORD_JANITOR);

      try
      {
         // Get 
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Invoke (should succeed, not fail with unauthorized errors)
         school.openServiceDoor();
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }
   }

   /**
    * Ensures that the "student" user cannot open the service door
    */
   @Test(expected = EJBAccessException.class)
   public void studentCannotOpenServiceDoor() throws NamingException
   {

      /*
       * This login and lookup code is specific to OpenEJB container
       */

      // Log in via JNDI as "student" user
      final Context context = this.login(USER_NAME_STUDENT, PASSWORD_STUDENT);

      try
      {
         // Get 
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Invoke (should fail)
         school.openServiceDoor();
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }
   }

   /**
    * Ensures that the "student" user cannot close the school (and go home early ;) )
    */
   @Test(expected = EJBAccessException.class)
   public void studentCannotCloseSchool() throws NamingException
   {

      /*
       * This login and lookup code is specific to OpenEJB container
       */

      // Log in via JNDI as "student" user
      final Context context = this.login(USER_NAME_STUDENT, PASSWORD_STUDENT);

      try
      {
         // Get 
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Invoke (should fail)
         school.close();
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }
   }

   /**
    * Ensures that the "admin" user can close the school
    */
   @Test
   public void adminCanCloseSchool() throws NamingException
   {

      /*
       * This login and lookup code is specific to OpenEJB container
       */

      // Log in via JNDI as "admin" user
      final Context context = this.login(USER_NAME_ADMIN, PASSWORD_ADMIN);

      try
      {
         // Get 
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Invoke (should succeed)
         school.close();

         // Test
         Assert.assertFalse("School should now be closed", school.isOpen());

         // Reset the school to open for subsequent tests
         school.open();

         // Test
         Assert.assertTrue("School should now be open", school.isOpen());
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }
   }

   /**
    * Ensures that an unauthenticated user can check if a school is open
    */
   @Test
   public void unauthenticatedUserCanCheckIfSchoolIsOpen()
   {

      // See if school is open
      Assert.assertTrue("Unauthenticated user should see that school is open", unauthenticatedSchool.isOpen());
   }

   /**
    * Ensures that a student cannot open the front door
    * when school is closed; tests programmatic security via 
    * {@link SessionContext} in the implementation class
    */
   @Test(expected = SchoolClosedException.class)
   public void studentCannotOpenFrontDoorsWhenSchoolIsClosed() throws Throwable
   {
      /*
       * This login and lookup code is specific to OpenEJB container
       */

      try
      {
         // Log in via JNDI as "admin" user
         final Context context = this.login(USER_NAME_ADMIN, PASSWORD_ADMIN);

         // Get
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Close the school
         school.close();

         // Log out
         context.close();

         // Test that we're closed
         Assert.assertFalse("School should now be closed", school.isOpen());

         // Now try to open the front doors as a student.  We do this in another Thread
         // because OpenEJB will associate the security context with this
         // Thread to "admin" (from above)
         final Callable<Void> studentOpenDoorTask = new Callable<Void>()
         {

            @Override
            public Void call() throws Exception
            {
               // Log in via JNDI as "student" user
               final Context context = SecureSchoolIntegrationTest.this.login(USER_NAME_STUDENT, PASSWORD_STUDENT);

               try
               {
                  // Get 
                  final SecureSchoolLocalBusiness school = SecureSchoolIntegrationTest.this.getEjb(context);

                  // Try to open the door (should fail)
                  school.openFrontDoor();

                  // Return
                  return null;
               }
               finally
               {
                  context.close();
               }
            }
         };
         final ExecutorService service = Executors.newSingleThreadExecutor();
         final Future<Void> future = service.submit(studentOpenDoorTask);
         try
         {
            future.get();// Should fail here
         }
         catch (final ExecutionException ee)
         {

            // Unwrap, should throw SchoolClosedException
            throw ee.getCause();
         }

      }
      finally
      {
         // Cleanup and open the school for other tests
         final Context context = this.login(USER_NAME_ADMIN, PASSWORD_ADMIN);
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Reset the school to open for subsequent tests
         school.open();

         // Test
         Assert.assertTrue("School should now be open", school.isOpen());

         // Clean up, closing the context to log out
         context.close();

      }
   }

   /**
    * Ensures that any unauthenticated user can declare an emergency, hence closing the school
    */
   @Test
   public void anyoneCanDeclareEmergencyAndCloseSchool() throws NamingException
   {

      // First check that school's open
      Assert.assertTrue("School should be open to start the test", unauthenticatedSchool.isOpen());

      // Ensure we can't close the school directly (we don't have access)
      boolean gotAccessException = false;
      try
      {
         unauthenticatedSchool.close();
      }
      catch (final EJBAccessException e)
      {
         // Expected
         log.info("We can't close the school on our own, make an emergency");
         gotAccessException = true;
      }
      Assert.assertTrue("We shouldn't be able to close school directly", gotAccessException);

      // Now declare an emergency via the fire department
      fireDepartment.declareEmergency();

      // The school should now be closed, even though we don't have rights to do that directly on our own.
      Assert.assertFalse("School should be closed after emergency was declared", unauthenticatedSchool.isOpen());

      // Reset the school to open
      // Cleanup and open the school for other tests
      final Context context = this.login(USER_NAME_ADMIN, PASSWORD_ADMIN);
      try
      {
         final SecureSchoolLocalBusiness school = this.getEjb(context);

         // Reset the school to open for subsequent tests
         school.open();

         // Test
         Assert.assertTrue("School should now be open", school.isOpen());
      }
      finally
      {
         // Clean up, closing the context to log out
         context.close();
      }

   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logs in to JNDI (and by extension, the EJB security system)
    * with the specified username and password.  This mechanism is
    * specific to the OpenEJB container.
    */
   private Context login(final String username, final String password)
   {
      // Precondition checks
      assert username != null : "username must be supplied";
      assert password != null : "password must be supplied";

      // Log in and create a context
      final Map<String, Object> namingContextProps = new HashMap<String, Object>();
      namingContextProps.put(Context.SECURITY_PRINCIPAL, username);
      namingContextProps.put(Context.SECURITY_CREDENTIALS, password);
      final Context context = arquillianContext.get(Context.class, namingContextProps);

      // Return
      return context;
   }

   /**
    * Obtains a proxy to the EJB via the specified JNDI Context (through
    * which the user may have authenticated)
    * @param context
    * @return
    * @throws NamingException
    */
   private SecureSchoolLocalBusiness getEjb(final Context context) throws NamingException
   {
      // Look up in JNDI specific to OpenEJB
      //TODO Use Global JNDI
      return (SecureSchoolLocalBusiness) context.lookup(JNDI_NAME_EJB);
   }
}
