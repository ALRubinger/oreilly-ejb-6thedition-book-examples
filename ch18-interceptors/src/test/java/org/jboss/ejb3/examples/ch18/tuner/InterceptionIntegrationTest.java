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
package org.jboss.ejb3.examples.ch18.tuner;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.interceptor.Interceptors;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration test ensuring that an EJB with {@link Interceptors} 
 * declared are intercepted when invoked
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@RunWith(Arquillian.class)
@Ignore //TODO Re-enable when EJB security in AS7 is available
public class InterceptionIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(InterceptionIntegrationTest.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Archive representing the deployment 
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      final JavaArchive deployment = ShrinkWrap.create(JavaArchive.class, "tuner.jar").addPackage(TunerBean.class.getPackage());
      log.info(deployment.toString(true));
      return deployment;
   }

   /**
    * The bean to invoke upon
    */
   @EJB(mappedName="java:module/TunerBean!org.jboss.ejb3.examples.ch18.tuner.TunerLocalBusiness")
   private static TunerLocalBusiness bean;
   
   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   
   /**
    * Cleanup
    */
   @After
   public void clearInvocationsAfterTest()
   {
      // Clean up
      CachingAuditor.clearInTesting();
   }
   
   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that invocation upon an EJB with {@link CachingAuditor} declared
    * results in the interception of targeted methods
    */
   @Test
   public void testCachingInterception() throws NamingException, IOException
   {
      // Ensure no invocations intercepted yet
      TestCase.assertEquals("No invocations should have yet been intercepted", 0, CachingAuditor.getInvocations()
            .size());

      // Invoke
      final int channel = 1;
      final InputStream content = bean.getChannel(channel);

      // Test the response is as expected
      TestCase.assertEquals("Did not obtain expected response", channel, content.read());

      // Test the invocation was intercepted 
      TestCase.assertEquals("The invocation should have been intercepted", 1, CachingAuditor.getInvocations().size());
   }

   /**
    * Ensures that requests to obtain Channel 2 while restricted are blocked with {@link Channel2ClosedException}
    */
   @Test(expected = Channel2ClosedException.class)
   public void testChannel2Restricted() throws Throwable
   {
      // Set the policy to block channel 2
      Channel2AccessPolicy.setChannel2Permitted(false);

      // Invoke
      try
      {
         bean.getChannel(2);
      }
      // Expected
      catch (final EJBException ejbe)
      {
         throw ejbe.getCause();
      }
      catch (final UndeclaredThrowableException ute)
      {
         throw ute.getCause();
      }

      // Fail if we reach here
      TestCase.fail("Request should have been blocked");
   }

   /**
    * Ensures that requests to obtain Channel 2 while open succeed
    */
   @Test
   public void testChannel2Allowed() throws NamingException, IOException
   {
      // Set the policy to block channel 2
      Channel2AccessPolicy.setChannel2Permitted(true);

      // Invoke
      final int channel = 2;
      final InputStream stream = bean.getChannel(channel);

      // Test
      TestCase.assertEquals("Unexpected content obtained from channel " + channel, channel, stream.read());
   }
}
