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
package org.jboss.ejb3.examples.ch07.rsscache.impl.rome;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Package-private utilities to protect against mutable
 * state getting exported
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
class ProtectExportUtil
{
   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Internal constructor; protects against instantiation
    */
   private ProtectExportUtil()
   {
   }

   //-------------------------------------------------------------------------------------||
   // Functional Methods -----------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Returns a copy of the specified URL; used to ensure that mutable 
    * internal state is not leaked out to clients
    * @param url
    * @return
    */
   static URL copyUrl(final URL url)
   {
      // If null, return
      if (url == null)
      {
         return url;
      }

      try
      {
         // Copy 
         return new URL(url.toExternalForm());
      }
      catch (final MalformedURLException e)
      {
         throw new RuntimeException("Error in copying URL", e);
      }
   }
}
