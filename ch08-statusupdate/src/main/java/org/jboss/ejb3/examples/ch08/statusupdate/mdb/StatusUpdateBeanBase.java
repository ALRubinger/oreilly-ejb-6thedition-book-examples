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
package org.jboss.ejb3.examples.ch08.statusupdate.mdb;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;

/**
 * Base support for the StatusUpdateEJB.  Responsible for
 * consuming an incoming JMS Message and dispatching to 
 * {@link StatusUpdateBeanBase#updateStatus(StatusUpdate)}.  Children
 * are required to supply specialization of this method.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public abstract class StatusUpdateBeanBase implements MessageListener
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(StatusUpdateBeanBase.class.getName());

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Updates status to the specified value.  
    * 
    * @throws IllegalArgumentException If the new status is not specified
    * @throws Exception If an error occured in processing
    */
   public abstract void updateStatus(StatusUpdate newStatus) throws IllegalArgumentException, Exception;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
    */
   @Override
   public void onMessage(final Message message)
   {
      /*
       * Precondition checks
       * 
       */
      // Ensure the message is specified
      if (message == null)
      {
         throw new IllegalArgumentException("Message must be specified");
      }

      // Ensure the message is in expected form
      final ObjectMessage objMessage;
      if (message instanceof ObjectMessage)
      {
         objMessage = (ObjectMessage) message;
      }
      else
      {
         throw new IllegalArgumentException("Specified message must be of type " + ObjectMessage.class.getName());
      }

      // Extract out the embedded status update
      final Serializable obj;
      try
      {
         obj = objMessage.getObject();
      }
      catch (final JMSException jmse)
      {
         throw new IllegalArgumentException("Could not obtain contents of message " + objMessage);
      }

      // Ensure expected type
      final StatusUpdate status;
      if (obj instanceof StatusUpdate)
      {
         status = (StatusUpdate) obj;
      }
      else
      {
         throw new IllegalArgumentException("Contents of message should be of type " + StatusUpdate.class.getName()
               + "; was instead " + obj);
      }

      // Process the update
      try
      {
         this.updateStatus(status);
      }
      catch (final Exception e)
      {
         throw new RuntimeException("Encountered problem with processing status update " + status, e);
      }
   }
}
