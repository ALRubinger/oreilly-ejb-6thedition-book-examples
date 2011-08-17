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

import java.security.Identity;
import java.security.Principal;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.interceptor.InvocationContext;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

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
public class CachingInterceptorUnitTestCase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CachingInterceptorUnitTestCase.class.getName());

   /**
    * Name of the mock user
    */
   private static String NAME_PRINCIPAL = "Mock User";

   /**
    * Principal to return
    */
   private Principal PRINCIPAL = new Principal()
   {

      @Override
      public String getName()
      {
         return NAME_PRINCIPAL;
      }
   };

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
      // Manually set the EJBContext to a mock view which only supports returning a principal
      interceptor.beanContext = new SessionContext()
      {

         /**
          * Exception to throw if we invoke any method aside from getCallerPrincipal
          */
         private UnsupportedOperationException UNSUPPORTED = new UnsupportedOperationException(
               "Not supported in mock implementation");

         @Override
         public void setRollbackOnly() throws IllegalStateException
         {
            throw UNSUPPORTED;

         }

         @Override
         public Object lookup(String arg0) throws IllegalArgumentException
         {
            throw UNSUPPORTED;
         }

         @Override
         public boolean isCallerInRole(String arg0)
         {
            throw UNSUPPORTED;
         }

         @Override
         @SuppressWarnings("deprecation")
         public boolean isCallerInRole(Identity arg0)
         {
            throw UNSUPPORTED;
         }

         @Override
         public UserTransaction getUserTransaction() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public TimerService getTimerService() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public boolean getRollbackOnly() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public Properties getEnvironment()
         {
            throw UNSUPPORTED;
         }

         @Override
         public EJBLocalHome getEJBLocalHome()
         {
            throw UNSUPPORTED;
         }

         @Override
         public EJBHome getEJBHome()
         {
            throw UNSUPPORTED;
         }

         @Override
         public Principal getCallerPrincipal()
         {
            return PRINCIPAL;
         }

         @Override
         @SuppressWarnings("deprecation")
         public Identity getCallerIdentity()
         {
            throw UNSUPPORTED;
         }

         @Override
         public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public EJBLocalObject getEJBLocalObject() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public EJBObject getEJBObject() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public Class<?> getInvokedBusinessInterface() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public MessageContext getMessageContext() throws IllegalStateException
         {
            throw UNSUPPORTED;
         }

         @Override
         public Map<String, Object> getContextData() {
            throw UNSUPPORTED;
         }

         @Override
         public boolean wasCancelCalled() throws IllegalStateException {
            throw UNSUPPORTED;
         }
      };
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
      final InvocationContext invocation = new MockInvocationContext(TunerLocalBusiness.class.getMethods()[0],
            new Object[]
            {1});
      interceptor.audit(invocation);

      // Test our invocation was cached properly
      TestCase.assertEquals("Cache should have the first invocation", 1, CachingAuditor.getInvocations().size());
      final AuditedInvocation audit = CachingAuditor.getInvocations().get(0);
      TestCase.assertEquals("Invocation cached was not the one that was invoked", invocation, audit.getContext());
      TestCase.assertEquals("Invocation did not store the caller as expected", PRINCIPAL, audit.getCaller());
   }

}
