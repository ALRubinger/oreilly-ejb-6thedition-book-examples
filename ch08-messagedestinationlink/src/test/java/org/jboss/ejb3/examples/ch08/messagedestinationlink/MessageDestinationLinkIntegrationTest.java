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

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.naming.Context;

import junit.framework.TestCase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.mdb.MessageDestinationLinkMdb;
import org.jboss.ejb3.examples.ch08.messagedestinationlink.slsb.MessageSendingBusiness;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Ensures that a SLSB wired to an MDB by way of a logical mapping
 * (message-destination-link) is working as expected
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
@Ignore // TODO Re-enable on AS7
public class MessageDestinationLinkIntegrationTest
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MessageDestinationLinkIntegrationTest.class.getName());

   /**
    * Name of the archive we'll deploy into the server for testing
    */
   private static final String NAME_MDB_ARCHIVE = "messageDestinationLink.jar";
   
   /**
    * Name of the file containing the Queue
    */
   private static final String QUEUE_DEPLOYMENT_NAME = "hornet-jms.xml";
   
   /**
    * The JNDI Context
    */
   private static Context NAMING_CONTEXT;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates the test archive
    */
   @Deployment
   public static JavaArchive createDeployment()
   {
      // Package up the EJBs and a Deployment Descriptor
//      final JavaArchive archive = ShrinkWrap
//            .create(JavaArchive.class, NAME_MDB_ARCHIVE)
//            .addClasses(MessageDestinationLinkConstants.class, MessageDestinationLinkMdb.class,
//                  MessageSendingBusiness.class, MessageSendingBean.class)
//            .addResource(QUEUE_DEPLOYMENT_NAME, "queues/" + QUEUE_DEPLOYMENT_NAME);
      
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addAsResource(QUEUE_DEPLOYMENT_NAME,
            "queues/" + QUEUE_DEPLOYMENT_NAME);

      // Log
      log.info("Deploying archive: " + archive.toString(true));

      // Return
      return archive;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that invoking an SLSB which sends messages to
    * a logical name (message-destination-link) works correctly
    * where the target MDB receives the message in question
    */
   @Test
   public void testMessageLinkingFromSlsbToMdb() throws Exception
   {
      // Obtain the EJB
      final MessageSendingBusiness bean = (MessageSendingBusiness) NAMING_CONTEXT
            .lookup(MessageSendingBusiness.NAME_JNDI);

      // Send a message
      final String message = "Testing Message Linking";
      bean.sendMessage(message);

      // Wait for the MDB to process, as it's doing so in another Thread.
      // This is *only* possible when we test MDBs in the same JVM as the test.
      final boolean processed;
      try
      {
         log.info("Waiting on the MDB...");
         processed = MessageDestinationLinkMdb.LATCH.await(10, TimeUnit.SECONDS);
      }
      catch (final InterruptedException e)
      {
         // Clear the flag and rethrow; some error in setup is in play
         Thread.interrupted();
         throw new RuntimeException(
               "Thread was interrupted while waiting for MDB processing; should not happen in this test");
      }

      // Ensure the MDB processed the message
      if (!processed)
      {
         TestCase.fail("The MDB did not process the message in the allotted time.");
      }
      log.info("MDB signaled it's done processing, so we can resume");

      // Ensure the contents are as expected
      final String roundtrip = MessageDestinationLinkMdb.LAST_MESSAGE;
      TestCase.assertEquals("Last message sent was not as expected", message, roundtrip);
   }

}
