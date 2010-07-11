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

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.jboss.ejb3.examples.ch18.tuner.Channel2AccessPolicy;
import org.jboss.ejb3.examples.ch18.tuner.Channel2ClosedException;
import org.jboss.ejb3.examples.ch18.tuner.Channel2Restrictor;
import org.jboss.ejb3.examples.ch18.tuner.TunerLocalBusiness;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests to ensure that the {@link Channel2Restrictor}
 * interceptor is working as expected outside the context
 * of a full container.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Channel2RestrictorUnitTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(Channel2RestrictorUnitTestCase.class.getName());

   /**
    * Method to get channel content
    */
   private static final Method METHOD_GET_CHANNEL = TunerLocalBusiness.class.getMethods()[0];

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The interceptor instance to test
    */
   private Channel2Restrictor interceptor;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates the interceptor instance to be used in testing
    */
   @Before
   public void createInterceptor()
   {
      interceptor = new Channel2Restrictor();
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures requests for channel 2 are blocked when the channel's access is closed
    */
   @Test(expected = Channel2ClosedException.class)
   public void requestsToChannel2Blocked() throws Exception
   {
      // Set the access policy to block
      Channel2AccessPolicy.setChannel2Permitted(false);

      // Invoke
      final InvocationContext invocation = new MockInvocationContext(METHOD_GET_CHANNEL, new Object[]
      {2});
      interceptor.checkAccessibility(invocation);
   }

   /**
    * Ensures requests for channel 2 are not blocked when the channel's access is open
    */
   @Test
   public void requestsToChannel2NotBlocked() throws Exception
   {
      // Set the access policy to block
      Channel2AccessPolicy.setChannel2Permitted(true);

      // Invoke
      final InvocationContext invocation = new MockInvocationContext(METHOD_GET_CHANNEL, new Object[]
      {2});
      try
      {
         interceptor.checkAccessibility(invocation);
      }
      catch (final Channel2ClosedException e)
      {
         TestCase.fail("Should not have been blocked with: " + e);
      }
   }

   /**
    * Ensures requests for channel 1 are not blocked channel 2's access is closed
    */
   @Test
   public void requestsToChannel1NeverBlocked() throws Exception
   {
      // Set the access policy to block
      Channel2AccessPolicy.setChannel2Permitted(false);

      // Invoke
      final InvocationContext invocation = new MockInvocationContext(METHOD_GET_CHANNEL, new Object[]
      {1});
      interceptor.checkAccessibility(invocation);
   }

}
