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

import twitter4j.Twitter;

/**
 * A stateless class used in creating new instances of the {@link Twitter}
 * client.  In practice we'd never take this approach, where creation is 
 * dependent upon a username/password credential set obtained from the
 * environment.  In these examples we must both externalize these properties
 * such that the EJBs using them may be configured, but also hide 
 * default values for the sake of security.
 * 
 * It is not advised to take this approach in real systems.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class EnvironmentSpecificTwitterClientUtil
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Environment variable of the Twitter username
    */
   private static final String ENV_VAR_NAME_TWITTER_USERNAME = "OREILLY_EJB_BOOK_CH08_TWITTER_USERNAME";

   /**
    * Environment variable of the Twitter password 
    */
   private static final String ENV_VAR_NAME_TWITTER_PASSWORD = "OREILLY_EJB_BOOK_CH08_TWITTER_PASSWORD";

   /**
    * Message dictating that the environment does not support Twitter integration
    */
   static final String MSG_UNSUPPORTED_ENVIRONMENT = "Both environment variables \"" + ENV_VAR_NAME_TWITTER_USERNAME
         + "\" and \"" + ENV_VAR_NAME_TWITTER_PASSWORD + "\" must be specified for this test to run";

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private EnvironmentSpecificTwitterClientUtil()
   {
      throw new UnsupportedOperationException("No instantiation allowed");
   }

   //-------------------------------------------------------------------------------------||
   // Utility Methods --------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   static boolean isSupportedEnvironment()
   {
      // Obtain the username and password
      final UsernamePasswordCredentials creds = getCredentials();
      final String username = creds.username;
      final String password = creds.password;

      /*
       * Only continue if these are specified, otherwise log out a warning and skip this 
       * test.  Ordinarily you should NOT test based upon the environment, but in this 
       * case we cannot put username/password combinations in SVN due to security constraints, 
       * and this test interacts with an outside service which we do not control and cannot mock
       * locally (which would default the purpose of showing how MDBs can be used to asynchronously
       * integrate with other systems).
       * 
       * Typically we'd first enforce the executing environment, but we can't assume that 
       * all users of this example have a Twitter account.
       */
      if (username == null || password == null)
      {
         return false;
      }

      // All good
      return true;
   }

   /**
    * Obtains a Twitter client for the username and password as specified from 
    * the environment.  If the environment is not fully set up, an {@link IllegalStateException}
    * will be raised noting the environment variables expected to be in place.  To avoid the ISE
    * first check for the integrity of the environment by using 
    * {@link EnvironmentSpecificTwitterClientUtil#isSupportedEnvironment()}
    * 
    * @throws IllegalStateException If the environment does not support creation of a Twitter client
    */
   static Twitter getTwitterClient() throws IllegalStateException
   {
      // Obtain the username and password
      final String username = SecurityActions.getEnvironmentVariable(ENV_VAR_NAME_TWITTER_USERNAME);
      final String password = SecurityActions.getEnvironmentVariable(ENV_VAR_NAME_TWITTER_PASSWORD);

      /*
       * We're only supported if both the username and password have been set
       */
      if (!isSupportedEnvironment())
      {
         throw new IllegalStateException(MSG_UNSUPPORTED_ENVIRONMENT);
      }

      // Get a Twitter client
      final Twitter twitterClient = new Twitter(username, password);

      // Return
      return twitterClient;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the username/password credentials from the environment
    * @return
    */
   private static UsernamePasswordCredentials getCredentials()
   {
      // Obtain the username and password
      final String username = SecurityActions.getEnvironmentVariable(ENV_VAR_NAME_TWITTER_USERNAME);
      final String password = SecurityActions.getEnvironmentVariable(ENV_VAR_NAME_TWITTER_PASSWORD);

      // Return as unified view
      final UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
      creds.username = username;
      creds.password = password;
      return creds;
   }

   //-------------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Simple value object to encapsulate a username/password pair
    *
    * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
    * @version $Revision: $
    */
   private static class UsernamePasswordCredentials
   {
      private String username;

      private String password;
   }
}
