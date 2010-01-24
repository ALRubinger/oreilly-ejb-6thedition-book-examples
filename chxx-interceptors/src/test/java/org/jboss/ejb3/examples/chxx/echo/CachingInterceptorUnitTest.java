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
package org.jboss.ejb3.examples.chxx.echo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import javax.interceptor.InvocationContext;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests to ensure that the {@link CachingAuditor}
 * interceptor is working as expected outside the context
 * of a full container.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CachingInterceptorUnitTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CachingInterceptorUnitTest.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The interceptor instance to test
    */
   private CachingAuditor interceptor;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates the interceptor instance to be used in testing
    */
   @Before
   public void createInterceptor()
   {
      interceptor = new CachingAuditor();
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that contexts passed through the interceptor are cached
    */
   @Test
   public void testCache() throws Exception
   {
      // Ensure the cache is empty to start
      TestCase.assertEquals("Cache should start empty", 0, CachingAuditor.getInvocations().size());

      // Invoke
      final InvocationContext invocation = new MockInvocationContext();
      interceptor.audit(invocation);

      // Test our invocation was cached properly
      TestCase.assertEquals("Cache should have the first invocation", 1, CachingAuditor.getInvocations().size());
      TestCase.assertEquals("Invocation cached was not the one that was invoked", invocation, CachingAuditor
            .getInvocations().get(0));
   }

   //-------------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@link InvocationContext} implementation which throws {@link UnsupportedOperationException}
    * for all required methods except {@link InvocationContext#proceed()}, which will always return null.
    */
   private static class MockInvocationContext implements InvocationContext
   {

      /**
       * Message used to denote that the operation is not supported 
       */
      private static final String MSG_UNSUPPORTED = "Not supported in mock implementation";

      @Override
      public Map<String, Object> getContextData()
      {
         throw new UnsupportedOperationException(MSG_UNSUPPORTED);
      }

      @Override
      public Method getMethod()
      {
         throw new UnsupportedOperationException(MSG_UNSUPPORTED);
      }

      @Override
      public Object[] getParameters()
      {
         throw new UnsupportedOperationException(MSG_UNSUPPORTED);
      }

      @Override
      public Object getTarget()
      {
         throw new UnsupportedOperationException(MSG_UNSUPPORTED);
      }

      @Override
      public Object proceed() throws Exception
      {
         return null;
      }

      @Override
      public void setParameters(Object[] arg0)
      {
         throw new UnsupportedOperationException(MSG_UNSUPPORTED);
      }

   }
}
