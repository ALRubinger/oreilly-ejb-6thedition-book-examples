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
package org.jboss.ejb3.examples.ch06.filetransfer;

import java.io.File;

import javax.ejb.NoSuchEJBException;
import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * FileTransferIntegrationTestCase
 * 
 * Test cases to ensure that the FileTransferEJB is working as
 * a Stateful Session Bean from the EJB Container.
 * 
 * Inherits some test support from {@link FileTransferTestCaseBase},
 * and additionally tests EJB-specific tasks upon the 
 * proxy.  Shows that sessions operate in isolation, and that removal
 * of a session means you cannot use it anymore.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class FileTransferIntegrationTestCase extends FileTransferTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferIntegrationTestCase.class);

   /**
    * JNDI Name to which the FileTransferEJB is bound
    */
   //TODO Use Global JNDI Name (not yet available in JBoss EJB3)
   private static final String JNDI_NAME_FILETRANSFER_EJB = FileTransferBean.EJB_NAME + "/remote";

   /**
    * Naming context used for lookups
    */
   private static Context namingContext;

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Our view of the EJB, remote business interface type of the Proxy
    */
   private FileTransferRemoteBusiness client;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates and sets the JNDI Naming Context used to look up SFSB Proxies
    */
   @BeforeClass
   public static void createNamingContext() throws Exception
   {
      // Create
      final Context context = new InitialContext(); // Properties from ClassPath jndi.properties

      // Log and set
      log.info("Created JNDI Context: " + context);
      namingContext = context;
   }

   /**
    * Obtains and sets the FTP Client SFSB Proxy
    */
   @Before
   public void obtainClient() throws Exception
   {
      // Set
      client = this.createNewSession();
   }

   /**
    * Ends the session upon the FTP Client SFSB Proxy 
    * and resets
    */
   @After
   public void endClientSession() throws Exception
   {
      // End the session
      try
      {
         client.endSession();
      }
      // If we've already been ended
      catch (final NoSuchEJBException nsee)
      {
         // Ignore
      }

      // Clear
      client = null;
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that two separate sessions will act in isolation from each other
    * 
    * @throws Exception
    */
   @Test
   public void testSessionIsolation() throws Exception
   {
      // Log
      log.info("testSessionIsolation");

      // Get the existing client as made from the test lifecycle
      final FileTransferRemoteBusiness session1 = this.getClient();

      // Make a new session and use that as another client
      final FileTransferRemoteBusiness session2 = this.createNewSession();

      // cd into a home directory for each
      final String ftpHome = getFtpHome().getAbsolutePath();
      session1.cd(ftpHome);
      session2.cd(ftpHome);

      // Now make a new directory for each session, and go into it
      final String newDirSession1 = "newDirSession1";
      final String newDirSession2 = "newDirSession2";
      session1.mkdir(newDirSession1);
      session1.cd(newDirSession1);
      session2.mkdir(newDirSession2);
      session2.cd(newDirSession2);

      // Get the current working directory for each session
      final String pwdSession1 = session1.pwd();
      final String pwdSession2 = session2.pwd();

      // Ensure each session is in the proper working directory
      TestCase.assertEquals("Session 1 is in unexpected pwd", ftpHome + File.separator + newDirSession1, pwdSession1);
      TestCase.assertEquals("Session 2 is in unexpected pwd", ftpHome + File.separator + newDirSession2, pwdSession2);

      // End the session manually for session2 (session1 will be ended by test lifecycle)
      session2.endSession();
   }

   /**
    * Tests that a call to {@link FileTransferRemoteBusiness#endSession()}
    * results in the SFSB's backing instance removal, and that subsequent 
    * operations result in a {@link NoSuchEJBException}
    * 
    * @throws Exception
    */
   @Test
   public void testSfsbRemoval() throws Exception
   {
      // Log
      log.info("testSfsbRemoval");

      // Get the existing client as made from the test lifecycle
      final FileTransferRemoteBusiness sfsb = this.getClient();

      // cd into the home directory
      final String ftpHome = getFtpHome().getAbsolutePath();
      sfsb.cd(ftpHome);

      // Get and test the pwd
      final String pwdBefore = sfsb.pwd();
      TestCase.assertEquals("Session should be in the FTP Home directory", ftpHome, pwdBefore);

      // End the session, resulting in an underlying instance
      // removal due to the annotation with @Remove upon 
      // the bean implementation class
      sfsb.endSession();

      // Now try some other operation, and ensure that we get a NoSuchEJBException
      boolean gotExpectedException = false;
      try
      {
         // This should not succeed, because we've called a method marked as @Remove
         sfsb.pwd();
      }
      catch (final NoSuchEJBException nsee)
      {
         gotExpectedException = true;
      }
      TestCase.assertTrue("Call to end the session did not result in underlying removal of the SFSB bean instance",
            gotExpectedException);
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch06.filetransfer.FileTransferTestCaseBase#getClient()
    */
   @Override
   protected FileTransferRemoteBusiness getClient()
   {
      return this.client;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the SFSB Proxy from JNDI, creating a new user session
    */
   private FileTransferRemoteBusiness createNewSession() throws Exception
   {
      // Look up in JNDI
      final String jndiName = JNDI_NAME_FILETRANSFER_EJB;
      final Object proxy = namingContext.lookup(jndiName);
      log.info("Obtained FTP EJB Proxy from JNDI at \"" + jndiName + "\" : " + proxy);

      // Return
      return (FileTransferRemoteBusiness) proxy;
   }

}
