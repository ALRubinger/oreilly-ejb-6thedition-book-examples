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
package org.jboss.ejb3.examples.ch05.encryption;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests to ensure that the business methods of the EncryptionEJB
 * are working as expected
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 */
public class EncryptionUnitTestCase extends EncryptionTestCaseSupport
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger 
    */
   private static final Logger log = Logger.getLogger(EncryptionUnitTestCase.class.getName());

   /**
    * POJO Encryption Service
    */
   private static EncryptionBean encryptionService;

   // ---------------------------------------------------------------------------||
   // Lifecycle -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Initializes the suite, invoked once before any tests are run 
    */
   @BeforeClass
   public static void initialize() throws Throwable
   {
      // Create the encryption service as a POJO
      encryptionService = new EncryptionBean();
      encryptionService.initialize(); // We call init manually here
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * These tests will use the POJO set up in test initialization
    */

   /**
    * @see {@link EncryptionTestCaseSupport#assertHashing(EncryptionCommonBusiness)}
    */
   @Test
   public void testHashing() throws Throwable
   {
      // Log
      log.info("testHashing");

      // Test via superclass
      this.assertHashing(encryptionService);
   }

   /**
    * @see {@link EncryptionTestCaseSupport#assertEncryption(EncryptionCommonBusiness)}
    */
   @Test
   public void testEncryption() throws Throwable
   {
      // Log
      log.info("testEncryption");

      // Test via superclass
      this.assertEncryption(encryptionService);
   }
}
