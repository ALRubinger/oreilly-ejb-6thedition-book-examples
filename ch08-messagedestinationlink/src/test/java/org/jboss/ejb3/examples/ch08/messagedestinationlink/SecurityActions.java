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
package org.jboss.ejb3.examples.ch08.messagedestinationlink;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Protected security actions not to leak outside this package
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
class SecurityActions
{

   //-------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * No external instanciation
    */
   private SecurityActions()
   {

   }

   //-------------------------------------------------------------------------------||
   // Utility Methods --------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * Obtains the Thread Context ClassLoader
    */
   static ClassLoader getThreadContextClassLoader()
   {
      return AccessController.doPrivileged(GetTcclAction.INSTANCE);
   }

   /**
    * Sets the specified CL upon the current Thread's Context 
    * 
    * @param cl
    * @throws IllegalArgumentException If the CL was null
    */
   static void setThreadContextClassLoader(final ClassLoader cl) throws IllegalArgumentException
   {
      if (cl == null)
      {
         throw new IllegalArgumentException("ClassLoader was null");
      }

      AccessController.doPrivileged(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            Thread.currentThread().setContextClassLoader(cl);
            return null;
         };
      });
   }

   /**
    * Obtains the system property with the specified key
    * 
    * @param key
    * @return
    * @throws IllegalArgumentException If the key is null
    */
   static String getSystemProperty(final String key) throws IllegalArgumentException
   {
      // Precondition check
      if (key == null)
      {
         throw new IllegalArgumentException("key was null");
      }

      // Get sysprop
      return AccessController.doPrivileged(new GetSystemPropertyAction(key));
   }

   //-------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------||
   //-------------------------------------------------------------------------------||

   /**
    * {@link PrivilegedAction} action to obtain the TCCL
    */
   private enum GetTcclAction implements PrivilegedAction<ClassLoader> {
      INSTANCE;

      @Override
      public ClassLoader run()
      {
         return Thread.currentThread().getContextClassLoader();
      }
   }

   /**
    * {@link PrivilegedAction} to access a system property
    * 
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private static class GetSystemPropertyAction implements PrivilegedAction<String>
   {

      /**
       * Name of the sysprop to get
       */
      private String sysPropName;

      /**
       * Creates a new instance capable of obtaining the specified system property by name
       * @param sysPropName
       */
      public GetSystemPropertyAction(final String sysPropName)
      {
         this.sysPropName = sysPropName;
      }

      /**
       * {@inheritDoc}
       * @see java.security.PrivilegedAction#run()
       */
      @Override
      public String run()
      {
         return System.getProperty(sysPropName);
      }
   }

}
