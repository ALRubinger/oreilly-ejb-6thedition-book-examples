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
package org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.jboss.ejb3.examples.ch08.messagedestinationlink.api.MessageDestinationLinkConstants;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.mdb.MessageDestinationLinkMdb;

/**
 * Stateless Sesssion Bean which sends a JMS {@link TextMessage} to
 * the {@link MessageDestinationLinkMdb} by way of a "message-destination-link"
 * as configured in the deployment descriptor.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless(name = MessageSendingBusiness.NAME_EJB)
@Local(MessageSendingBusiness.class)
public class MessageSendingBean implements MessageSendingBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MessageSendingBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Queue we'll send messages to; logical name as wired from the message-destination-link
    */
   @Resource(name = MessageDestinationLinkConstants.NAME_MESSAGE_DESTINATION_LINK_REF, mappedName = "queue/MessageDestinationLinkQueue")
   // Name to match message-destination-ref-name
   private Queue queue;

   /**
    * Connection factory for making new Queue connections
    */
   @Resource(name = MessageDestinationLinkConstants.JNDI_NAME_CONNECTION_FACTORY, mappedName = MessageDestinationLinkConstants.JNDI_NAME_CONNECTION_FACTORY)
   private QueueConnectionFactory connectionFactory;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb.MessageSendingBusiness#sendMessage(java.lang.String)
    */
   @Override
   public void sendMessage(final String contents)
   {
      // Precondition checks
      if (contents == null || contents.length() == 0)
      {
         throw new IllegalArgumentException("contents must be specified");
      }

      // Create a connection
      final QueueConnection connection;
      try
      {
         connection = connectionFactory.createQueueConnection();
      }
      catch (final JMSException jmse)
      {
         throw new RuntimeException("Could not create new connection", jmse);
      }

      // Create a session
      final QueueSession session;
      try
      {
         session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
      }
      catch (final JMSException jmse)
      {
         throw new RuntimeException("Could not create new session", jmse);
      }

      // Create a sender
      final QueueSender sender;
      try
      {
         sender = session.createSender(queue);
      }
      catch (final JMSException jmse)
      {
         throw new RuntimeException("Could not create new sender", jmse);
      }

      // Create a message
      final TextMessage message;
      try
      {
         message = session.createTextMessage(contents);
      }
      catch (final JMSException jmse)
      {
         throw new RuntimeException("Could not create new message", jmse);
      }

      // Send
      try
      {
         sender.send(message);
         log.info("Sent to MDB: " + message);
      }
      catch (final JMSException jmse)
      {
         throw new RuntimeException("Could not send message", jmse);
      }
      
      try
      {
         session.close();
      }
      catch (JMSException ex)
      {
         Logger.getLogger(MessageSendingBean.class.getName()).log(Level.SEVERE, null, ex);
      }
      try
      {
         connection.close();
      }
      catch (JMSException ex)
      {
         Logger.getLogger(MessageSendingBean.class.getName()).log(Level.SEVERE, null, ex);
      }

   }
}
