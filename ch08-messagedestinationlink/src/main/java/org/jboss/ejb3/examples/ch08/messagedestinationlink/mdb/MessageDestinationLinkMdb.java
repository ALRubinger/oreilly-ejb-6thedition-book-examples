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
package org.jboss.ejb3.examples.ch08.messagedestinationlink.mdb;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * MDB which is linked to a SLSB via the deployment descriptor; obtains messages
 * and caches them as a class member for obtaining from a test later
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@MessageDriven(name = MessageDestinationLinkMdb.NAME_EJB, activationConfig =
{@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/MessageDestinationLinkQueue"),
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")})
public class MessageDestinationLinkMdb implements MessageListener
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MessageDestinationLinkMdb.class.getName());

   /**
    * Shared latch, so tests can wait until the MDB is processed.  In POJO
    * testing this is wholly unnecessary as we've got a single-threaded environment, but 
    * when testing in an EJB Container running in the *same* JVM as the test, the test 
    * can use this to wait until the MDB has been invoked, strengthening the integrity
    * of the test.  It's not recommended to put this piece into a production EJB; instead
    * test an extension of your EJB which adds this (and only this) support.
    */
   public static CountDownLatch LATCH = new CountDownLatch(1);

   /**
    * Name of this EJB, to match the ejb-name used in the deployment descriptor
    */
   static final String NAME_EJB = "MessageDestinationLinkMdb";

   /**
    * Last message received; never use this in production (as many Threads/instances
    * may concurrently have access).  Here we do so safely within the confines
    * of our test environment.
    */
   public static String LAST_MESSAGE;

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
      // Cast
      if (!(message instanceof TextMessage))
      {
         throw new IllegalArgumentException("Expecting message of type " + TextMessage.class.getName() + "; got: "
               + message);
      }

      final TextMessage txtMessage = (TextMessage) message;
      final String contents;
      try
      {
         contents = txtMessage.getText();
      }
      catch (final JMSException e)
      {
         throw new RuntimeException("Could not get contents of message: " + txtMessage, e);
      }
      log.info("Received message with contents: " + contents);
      LAST_MESSAGE = contents;

      // Count down the latch so that the test knows we're here
      log.info("Counting down the latch...");
      LATCH.countDown();

   }

}
