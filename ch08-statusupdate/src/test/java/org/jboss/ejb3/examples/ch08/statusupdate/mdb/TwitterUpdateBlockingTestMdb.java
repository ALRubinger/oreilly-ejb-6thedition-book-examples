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
package org.jboss.ejb3.examples.ch08.statusupdate.mdb;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdateConstants;

/**
 * Extends the {@link TwitterUpdateMdb} example to add a latch to
 * be shared in testing only, such that tests can be sure we're done 
 * processing before they proceed
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@MessageDriven(name = TwitterUpdateMdb.NAME, activationConfig =
{
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = StatusUpdateConstants.TYPE_DESTINATION_STATUSUPDATE),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = StatusUpdateConstants.JNDI_NAME_TOPIC_STATUSUPDATE)})
public class TwitterUpdateBlockingTestMdb extends TwitterUpdateMdb implements MessageListener
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TwitterUpdateBlockingTestMdb.class.getName());

   /**
    * Shared latch, so tests can wait until the MDB is processed.  In POJO
    * testing this is wholly unnecessary as we've got a single-threaded environment, but 
    * when testing in an EJB Container running in the *same* JVM as the test, the test 
    * can use this to wait until the MDB has been invoked, strengthening the integrity
    * of the test.  It's not recommended to put this piece into a production EJB; instead
    * test an extension of your EJB which adds this (and only this) support.
    */
   public static CountDownLatch LATCH = new CountDownLatch(1);

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * Additionally waits upon a shared barrier so that the test can ensure we're done before
    * it proceeds
    * @see org.jboss.ejb3.examples.ch08.statusupdate.mdb.TwitterUpdateMdb#updateStatus(org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate)
    */
   @Override
   public void updateStatus(final StatusUpdate newStatus) throws IllegalArgumentException, Exception
   {
      // Call the super implementation
      try
      {
         super.updateStatus(newStatus);
      }
      finally
      {
         // Count down the latch
         log.info("Counting down the latch...");
         LATCH.countDown();
      }

   }

}
