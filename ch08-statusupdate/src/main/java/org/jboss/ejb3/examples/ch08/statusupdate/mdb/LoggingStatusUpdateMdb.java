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

import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdateConstants;

/**
 * An MDB which, {@link MessageListener#onMessage(javax.jms.Message)}, will
 * log out the status update at INFO-level. 
 * 
 * Not explicitly tested by the examples (because we can't test for logging), 
 * but its usage should be illustrative.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@MessageDriven(activationConfig =
{
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = StatusUpdateConstants.TYPE_DESTINATION_STATUSUPDATE),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = StatusUpdateConstants.JNDI_NAME_TOPIC_STATUSUPDATE)})
public class LoggingStatusUpdateMdb extends StatusUpdateBeanBase implements MessageListener
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(LoggingStatusUpdateMdb.class.getName());

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logs status out at INFO-level
    * @see org.jboss.ejb3.examples.ch08.statusupdate.mdb.StatusUpdateBeanBase#updateStatus(org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate)
    */
   @Override
   public void updateStatus(final StatusUpdate newStatus) throws IllegalArgumentException, Exception
   {
      // Precondition checks
      if (newStatus == null)
      {
         throw new IllegalArgumentException("status must be specified");
      }

      // Get info
      final String status = newStatus.getText();

      // Log
      log.info("New status received: \"" + status + "\"");
   }

}
