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
import java.util.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

/**
 * Simple EJB which returns references back to the client.  Used to
 * show configuration of interceptors; here we've configured the 
 * {@link CachingAuditor} to remember all previous 
 * {@link InvocationContext}s made upon the EJB.  
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless
// Class-level interceptors will be run upon requests to every method of this EJB
@Interceptors(CachingAuditor.class)
@Local(TunerLocalBusiness.class)
public class TunerBean implements TunerLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TunerBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch18.tuner.TunerLocalBusiness#getChannel(int)
    */
   // Here we declare method-level interceptors, which will only take place on this method
   @Interceptors(Channel2Restrictor.class)
   @Override
   public InputStream getChannel(final int channel) throws IllegalArgumentException
   {
      // Declare the stream we'll use
      final InputStream stream;
      switch (channel)
      {
         // We want channel 1
         case 1 :
            stream = new InputStream()
            {

               @Override
               public int read() throws IOException
               {
                  return 1;
               }
            };
            break;
         // We want channel 2
         case 2 :
            stream = new InputStream()
            {

               @Override
               public int read() throws IOException
               {
                  return 2;
               }
            };
            break;
         // We've requested an improper channel
         default :
            throw new IllegalArgumentException("Not a valid channel: " + channel);
      }

      // Return
      log.info("Returning stream for Channel " + channel + ": " + stream);
      return stream;
   }
}
