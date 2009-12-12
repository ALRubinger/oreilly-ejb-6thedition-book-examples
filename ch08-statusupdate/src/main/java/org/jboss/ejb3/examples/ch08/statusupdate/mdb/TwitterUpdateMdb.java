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

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;

import org.jboss.ejb3.annotation.Depends;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;
import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdateConstants;

import twitter4j.Twitter;

/**
 * EJB 3.x MDB which will act as an adaptor to the Twitter API, updating
 * Twitter status on incoming messages.
 * 
 * The environment must first support Twitter integration by way of a username/password
 * pair available from environment properties. {@link EnvironmentSpecificTwitterClientUtil} 
 * has more details.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @see http://twitter.com
 * @see http://yusuke.homeip.net/twitter4j/en/index.html
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
{
      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = StatusUpdateConstants.TYPE_DESTINATION_STATUSUPDATE),
      @ActivationConfigProperty(propertyName = "destination", propertyValue = StatusUpdateConstants.JNDI_NAME_TOPIC_STATUSUPDATE)})
@Depends(StatusUpdateConstants.OBJECT_NAME_TOPIC_STATUSUPDATE)
// Dependency matches the name in the topic descriptor XML
public class TwitterUpdateMdb extends StatusUpdateBeanBase implements MessageListener
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TwitterUpdateMdb.class.getName());

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
   // Constructors -----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new instance, required as no-arg ctor by specification
    */
   public TwitterUpdateMdb()
   {

   }

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Underlying client used in updating Twitter by calling upon its API
    */
   private Twitter client;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Lifecycle start to create the Twitter client from supplied environment properties,
    * if the environment has been configured to do so
    */
   @PostConstruct
   void createTwitterClient()
   {
      if (!EnvironmentSpecificTwitterClientUtil.isSupportedEnvironment())
      {
         log.warning(EnvironmentSpecificTwitterClientUtil.MSG_UNSUPPORTED_ENVIRONMENT);
         return;
      }

      // Create the client
      client = EnvironmentSpecificTwitterClientUtil.getTwitterClient();
      log.info("Created Twitter client " + client);
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sends incoming status updates to the Twitter account configured in the 
    * context properties.
    * 
    * @see org.jboss.ejb3.examples.ch08.statusupdate.mdb.StatusUpdateBeanBase#updateStatus(org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate)
    */
   @Override
   public void updateStatus(final StatusUpdate newStatus) throws IllegalArgumentException, Exception
   {
      // Ensure the client's been initialized (if the environment permits)
      if (!EnvironmentSpecificTwitterClientUtil.isSupportedEnvironment())
      {
         // Do nothing and get out
         return;
      }
      if (client == null)
      {
         throw new IllegalStateException("Twitter client has not been initialized");
      }

      // Extract status
      final String status = newStatus.getText();

      // Update status
      client.updateStatus(status);

      // Count down the latch so that the test knows we're here
      log.info("Counting down the latch...");
      LATCH.countDown();

   }
}
