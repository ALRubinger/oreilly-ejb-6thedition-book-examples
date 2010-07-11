/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Aspect which keeps a cache of all intercepted
 * invocations in a globally-accessible cache.
 * 
 * Though demonstrative for testing and learning purposes, this is a very
 * poor example of a real-world auditing mechanism.  In a production environment, 
 * the copy-on-write nature of the cache will degrade geometrically 
 * over time, and additionally we export mutable views 
 * (ie. {@link InvocationContext#setParameters(Object[])}) to callers
 * of {@link CachingAuditor#getInvocations()}.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class CachingAuditor
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(CachingAuditor.class.getName());

   /**
    * Cached invocations; must be in a thread-safe implementation because this member
    * is shared by all interceptor instances, which are linked to bean instances.  Though
    * each bean instance is guaranteed to be used by only one thread at once, many bean instances
    * may be executed concurrently.
    */
   private static final List<AuditedInvocation> invocations = new CopyOnWriteArrayList<AuditedInvocation>();

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The current EJB Context; will either be injected by the EJB Container or
    * manually populated by unit tests
    */
   @Resource
   SessionContext beanContext;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Caches the intercepted invocation in an auditable view such that
    * it may later be obtained
    */
   @AroundInvoke
   public Object audit(final InvocationContext invocationContext) throws Exception
   {
      // Precondition checks
      assert invocationContext != null : "Context was not specified";

      // Obtain the caller
      Principal caller;
      try
      {
         caller = beanContext.getCallerPrincipal();
      }
      catch (final NullPointerException npe)
      {
         caller = new Principal()
         {

            @Override
            public String getName()
            {
               return "Unauthenticated Caller";
            }
         };
      }

      // Create a new view
      final AuditedInvocation audit = new AuditedInvocation(invocationContext, caller);

      // Add the invocation to the cache
      invocations.add(audit);

      // Carry out the invocation, noting where we've intercepted before and after the call (around it)
      try
      {
         // Log
         log.info("Intercepted: " + invocationContext);

         // Return
         return invocationContext.proceed();
      }
      finally
      {
         // Log
         log.info("Done with: " + invocationContext);
      }

   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Returns a read-only view of the {@link InvocationContext}
    * cached by this interceptor
    */
   public static List<AuditedInvocation> getInvocations()
   {
      // Copy on export
      return Collections.unmodifiableList(invocations);
   }

   /**
    * Test-only hook to clear the invocations
    */
   static void clearInTesting()
   {
      invocations.clear();
   }
}
