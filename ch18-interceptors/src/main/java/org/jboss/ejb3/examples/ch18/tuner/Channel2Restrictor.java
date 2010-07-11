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

import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

/**
 * Aspect which restricts access to Channel 2 unless
 * the network has allowed broadcasting.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class Channel2Restrictor
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(Channel2Restrictor.class.getName());

   /**
    * Name of the method to request channel content
    */
   private static final String METHOD_NAME_GET_CHANNEL;
   static
   {
      METHOD_NAME_GET_CHANNEL = TunerLocalBusiness.class.getMethods()[0].getName();
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Examines the specified request to determine if the caller is attempting 
    * to obtain content for Channel 2.  If so, and Channel 2 is currently closed, 
    * will block the request, instead throwing {@link Channel2ClosedException}
    */
   @AroundInvoke
   public Object checkAccessibility(final InvocationContext context) throws Exception
   {
      // Precondition checks
      assert context != null : "Context was not specified";

      // See if we're requesting Channel 2
      if (isRequestForChannel2(context))
      {
         // See if Channel 2 is open
         if (!Channel2AccessPolicy.isChannel2Permitted())
         {
            // Block access
            throw Channel2ClosedException.INSTANCE;
         }
      }

      // Otherwise carry on
      return context.proceed();
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Determines whether or not the specified context represents a request for Channel 2
    */
   private static boolean isRequestForChannel2(final InvocationContext context)
   {
      // Precondition check
      assert context != null : "Context was not specified";

      // Get the target method
      final Method targetMethod = context.getMethod();

      // If we're requesting a new channel
      final String targetMethodName = targetMethod.getName();
      if (targetMethodName.equals(METHOD_NAME_GET_CHANNEL))
      {
         log.info("This is a request for channel content: " + context);
         // Get the requested channel
         final int channel = ((Integer) context.getParameters()[0]).intValue();
         if (channel == 2)
         {
            // Yep, they want channel 2
            return true;
         }
      }

      // Return
      return false;
   }
}
