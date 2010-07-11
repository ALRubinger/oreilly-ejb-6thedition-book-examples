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

import javax.annotation.PostConstruct;
import javax.jms.MessageListener;

import org.jboss.ejb3.examples.ch08.statusupdate.api.StatusUpdate;

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
 */
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
    * EJB Name
    */
   static final String NAME = "TwitterUpdateMdb";

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
   }
}
