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

import junit.framework.TestCase;

/**
 * Common base for centralizing test logic used
 * for the Encryption POJO and EncryptionEJB
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 */
public class EncryptionTestCaseSupport
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EncryptionTestCaseSupport.class.getName());

   /**
    * A simple String used in testing
    */
   private static final String TEST_STRING = "EJB 3.1 Examples Test String";

   // ---------------------------------------------------------------------------||
   // Test Support --------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Ensures that the hashing functions are working as expected:
    * 
    * 1) Passing through the hash returns a result inequal to the input
    * 2) Comparison upon the hash result and the original input matches 
    * 
    * @param service The service to use (either POJO or EJB)
    * @throws Throwable
    */
   protected void assertHashing(final EncryptionCommonBusiness service) throws Throwable
   {
      // Log
      log.info("assertHashing");

      // Declare the input
      final String input = TEST_STRING;

      // Hash
      final String hash = service.hash(input);
      log.info("Hash of \"" + input + "\": " + hash);

      // Test that the has function had some effect
      TestCase.assertNotSame("The hash function had no effect upon the supplied input", input, hash);

      // Get the comparison result
      final boolean equal = service.compare(hash, input);

      // Test that the input matches the hash we'd gotten
      TestCase.assertTrue("The comparison of the input to its hashed result failed", equal);
   }

   /**
    * Ensures that the encryption functions are working as expected:
    * 
    * 1) Passing through the encryption returns a result inequal to the input
    * 2) Round-trip through decryption again returns a result equal to the original input 
    * 
    * @param service The service to use (either POJO or EJB)
    * @throws Throwable
    */
   protected void assertEncryption(final EncryptionCommonBusiness service) throws Throwable
   {
      // Log
      log.info("assertEncryption");

      // Declare the input
      final String input = TEST_STRING;

      // Hash
      final String encrypted = service.encrypt(input);
      log.info("Encrypted result of \"" + input + "\": " + encrypted);

      // Test that the has function had some effect
      TestCase.assertNotSame("The encryption function had no effect upon the supplied input", input, encrypted);

      // Get the round-trip result
      final String roundTrip = service.decrypt(encrypted);

      // Test that the result matches the original input
      TestCase.assertEquals("The comparison of the input to its encrypted result failed", input, roundTrip);
   }
}
