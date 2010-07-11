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

import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import junit.framework.TestCase;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;
import org.junit.Test;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Unit tests for the StatusUpdate EJBs.  Ensures that the business 
 * logic is intact when running outside the context of an EJB Container.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class StatusUpdateUnitTestCase extends StatusUpdateTestBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(StatusUpdateUnitTestCase.class.getName());

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that the {@link StatusUpdateBeanBase#updateStatus(StatusUpdate)} method is
    * invoked for incoming messages
    */
   @Test
   public void testUpdateStatusBase()
   {

      // Make a listener
      final StatusCachingMessageListener listener = new StatusCachingMessageListener();

      // Make a status update
      final StatusUpdate newStatus = this.getUniqueStatusUpdate();

      // Send to it
      this.sendMessage(newStatus, listener);

      // Extract out the status sent
      final StatusUpdate roundtrip = listener.getLastStatus();

      // Ensure it's what we sent
      TestCase.assertEquals("Status sent was not dispatched and received as expected", newStatus, roundtrip);
   }

   /**
    * Ensures that the {@link TwitterUpdateMdb} is updating Twitter
    * when {@link MessageListener#onMessage(javax.jms.Message)} is invoked
    */
   @Test
   public void testTwitterUpdateMdb() throws TwitterException
   {
      // Determine if the environment is not set up
      if (!EnvironmentSpecificTwitterClientUtil.isSupportedEnvironment())
      {
         log.warning(EnvironmentSpecificTwitterClientUtil.MSG_UNSUPPORTED_ENVIRONMENT);
         return;
      }

      // Make a listener (the MDB bean impl class as a POJO)
      final TwitterUpdateMdb listener = new TwitterUpdateMdb();

      // Manually invoke @PostConstruct
      listener.createTwitterClient();

      // Make a status update
      final StatusUpdate newStatus = this.getUniqueStatusUpdate();

      // Send to it
      this.sendMessage(newStatus, listener);

      /*
       * NOTE: This test is flawed, as we do not update then check in one atomic operation.
       * Someone else could sneak in here and send another status update by the 
       * time we check the last one received.  This is for illustrative uses only.
       */

      // Test
      final Twitter twitterClient = EnvironmentSpecificTwitterClientUtil.getTwitterClient();
      this.assertLastUpdateSentToTwitter(twitterClient, newStatus);
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sends the specified status update to the specified listener 
    */
   private void sendMessage(final StatusUpdate newStatus, final MessageListener listener)
   {
      // This implementation will send directly (POJO-based)
      final ObjectMessage message = new MockObjectMessage(newStatus);

      // Send manually
      listener.onMessage(message);
   }

   //-------------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@link MessageListener} to be invoked in a POJO environment, where the last 
    * incoming status update via {@link MessageListener#onMessage(javax.jms.Message)}
    * is cached and available for retrieval.  Not thread-safe as this is intended to be used
    * in a single-threaded environment.
    */
   private static class StatusCachingMessageListener extends StatusUpdateBeanBase
   {
      private StatusUpdate lastStatus = null;

      /**
       * Caches the specified status
       * @see org.jboss.ejb3.examples.ch08.statusupdate.mdb.StatusUpdateBeanBase#updateStatus(org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate)
       */
      public void updateStatus(final StatusUpdate newStatus) throws IllegalArgumentException
      {
         this.lastStatus = newStatus;
      }

      /**
       * Obtains the last {@link StatusUpdate} received
       * @return
       */
      StatusUpdate getLastStatus()
      {
         return lastStatus;
      }
   }

}
