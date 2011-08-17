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
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Base tests for the file transfer test classes, may
 * be extended either from unit or integration tests.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public abstract class FileTransferTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(FileTransferTestCaseBase.class.getName());

   /**
    * The name of the directory under the writable temp filesystem which
    * will act as the home for these tests
    */
   private static final String RELATIVE_LOCATION_HOME = "ejb31_ch06-example-ftpHome";

   /**
    * The name of the system property denoting the I/O temp directory
    */
   private static final String SYS_PROP_NAME_IO_TMP_DIR = "java.io.tmpdir";

   /**
    * The File we'll use as the writeable home for FTP operations.  Created and
    * destroyed alongside test lifecycle.
    */
   private static File ftpHome;

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates the directory which we'll use as the writeable home 
    * for FTP operations; called before each test is run.
    * 
    * @throws Exception
    */
   @Before
   public void createFtpHome() throws Exception
   {
      final File ftpHome = getFtpHome();
      if (ftpHome.exists())
      {
         throw new RuntimeException("Error in test setup; FTP Home should not yet exist: " + ftpHome.getAbsolutePath());
      }
      final boolean created = ftpHome.mkdir();
      if (!created)
      {
         throw new RuntimeException("Request to create the FTP Home failed: " + ftpHome.getAbsolutePath());
      }
      log.info("Created FTP Home: " + ftpHome.getAbsolutePath());
   }

   /**
    * Removes the directory used as the writeable home 
    * for FTP operations; called after each test is run.
    * 
    * @throws Exception
    */
   @After
   public void deleteFtpHome() throws Exception
   {
      final File ftpHome = getFtpHome();
      if (!ftpHome.exists())
      {
         throw new RuntimeException("Error in test setup; FTP Home should exist: " + ftpHome.getAbsolutePath());
      }
      final boolean removed = this.deleteRecursive(ftpHome);
      if (!removed)
      {
         throw new RuntimeException("Request to remove the FTP Home failed: " + ftpHome.getAbsolutePath());
      }
      log.info("Removed FTP Home: " + ftpHome.getAbsolutePath());
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Tests that the a new directory can be made, we can switch 
    * into it, and we can obtain the present working directory of our newly-created
    * directory
    */
   @Test
   public void testMkdirCdAndPwd() throws Exception
   {
      // Log
      log.info("testMkdirAndPwd");

      // Get the client
      final FileTransferCommonBusiness client = this.getClient();

      // Switch to home
      final String home = getFtpHome().getAbsolutePath();
      client.cd(home);

      // Ensure we're home
      final String pwdBefore = client.pwd();
      TestCase.assertEquals("Present working directory should be our home", home, pwdBefore);

      // Make the directory
      final String newDir = "newDirectory";
      client.mkdir(newDir);

      // cd into the new dir
      client.cd(newDir);

      // Ensure we're in the new directory
      final String pwdAfter = client.pwd();
      TestCase.assertEquals("Present working directory should be our new directory", home + File.separator + newDir,
            pwdAfter);
   }

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the client to be used for the tests
    */
   protected abstract FileTransferCommonBusiness getClient();

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Recursively deletes all contents of the specified root, 
    * including the root itself.  If the specified root does not exist, 
    * no action is taken.
    * 
    * @param root
    * @return true if deleted, false otherwise
    */
   protected boolean deleteRecursive(final File root)
   {
      // Ensure exists
      if (!root.exists())
      {
         return false;
      }

      // Get all children
      final File[] children = root.listFiles();
      // If it's a directory
      if (children != null)
      {
         // Remove all children
         for (final File child : children)
         {
            this.deleteRecursive(child);
         }
      }

      // Delete me
      final boolean success = root.delete();
      log.info("Deleted: " + root);
      return success;
   }

   /**
    * Obtains the writeable home for these tests, set under the namespace of the
    * IO Temp directory
    */
   protected static File getFtpHome() throws Exception
   {
      // If the home is not defined
      if (ftpHome == null)
      {

         // Get the property
         final String sysPropIoTempDir = SYS_PROP_NAME_IO_TMP_DIR;
         final String ioTempDir = System.getProperty(sysPropIoTempDir);
         if (ioTempDir == null)
         {
            throw new RuntimeException("I/O temp directory was not specified by system property: " + sysPropIoTempDir);
         }

         // Make the File
         final File ioTempDirFile = new File(ioTempDir);
         if (!ioTempDirFile.exists())
         {
            throw new RuntimeException("I/O Temp directory does not exist: " + ioTempDirFile.getAbsolutePath());
         }

         // Append the suffix for our home
         final File home = new File(ioTempDirFile, RELATIVE_LOCATION_HOME);
         ftpHome = home;
      }

      // Return
      return ftpHome;
   }
}
