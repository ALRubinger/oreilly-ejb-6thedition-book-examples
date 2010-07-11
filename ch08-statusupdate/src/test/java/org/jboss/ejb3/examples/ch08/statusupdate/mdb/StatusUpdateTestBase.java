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

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Base support for tests of the StatusUpdate EJBs in both
 * POJO and JavaEE Environments
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class StatusUpdateTestBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(StatusUpdateBeanBase.class.getName());

   /**
    * Status update to send
    */
   private static final String STATUS_UPDATE_PREFIX_TWITTER = "I'm testing Message-Driven EJBs using JBoss EJB 3.x by @ALRubinger/@OReillyMedia!: ";

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that the last update for the Twitter account represented by the specified 
    * client matches the specified last sent status update
    * 
    * @throws TwitterException If an error occurred in obtaining the last update from the Twitter account
    */
   void assertLastUpdateSentToTwitter(final Twitter twitterClient, final StatusUpdate sent) throws TwitterException,
         IllegalArgumentException
   {
      // Precondition checks
      if (twitterClient == null)
      {
         throw new IllegalArgumentException("Twitter client must be specified");
      }
      if (sent == null)
      {
         throw new IllegalArgumentException("Last sent status must be specified");
      }

      // Get new status
      final List<Status> statuses = twitterClient.getUserTimeline(new Paging(1, 1));

      // Ensure we've sent one status, and it's as expected
      TestCase.assertEquals("Should have obtained one status (the most recent) back from request", 1, statuses.size());
      final String roundtrip = statuses.get(0).getText();
      final String expected = sent.getText();
      log.info("Sent status update to Twitter: " + expected);
      log.info("Got last status update from Twitter: " + roundtrip);
      TestCase.assertEquals("Twitter API did not update with last sent status", expected, roundtrip);
   }

   /**
    * Obtains a unique status update by using {@link StatusUpdateTestBase#STATUS_UPDATE_PREFIX_TWITTER}
    * prefixed to a UUID.
    */
   StatusUpdate getUniqueStatusUpdate()
   {
      return new StatusUpdate(STATUS_UPDATE_PREFIX_TWITTER + UUID.randomUUID().toString());
   }

}
