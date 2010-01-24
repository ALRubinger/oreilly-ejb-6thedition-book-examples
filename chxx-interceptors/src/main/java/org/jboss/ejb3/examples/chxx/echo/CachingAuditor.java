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
package org.jboss.ejb3.examples.chxx.echo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

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
   private static final List<InvocationContext> invocations = new CopyOnWriteArrayList<InvocationContext>();

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Caches the intercepted {@link InvocationContext} such that
    * a test may obtain it later
    */
   @AroundInvoke
   public Object audit(final InvocationContext context) throws Exception
   {
      // Precondition checks
      assert context != null : "Context was not specified";

      // Add the invocation to the cache
      invocations.add(context);

      // Carry out the invocation, noting where we've intercepted before and after the call (around it)
      try
      {
         // Log
         log.info("Intercepted: " + context);

         // Return
         return context.proceed();
      }
      finally
      {
         // Log
         log.info("Done with: " + context);
      }

   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Returns a read-only view of the {@link InvocationContext}
    * cached by this interceptor
    */
   public static List<InvocationContext> getInvocations()
   {
      // Copy on export
      return Collections.unmodifiableList(invocations);
   }
}
