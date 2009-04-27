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

import javax.naming.Context;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * EncryptionIntegrationTestCase
 * 
 * Integration tests for the EncryptionEJB
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EncryptionIntegrationTestCase extends EncryptionTestCaseSupport
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EncryptionIntegrationTestCase.class);

   /**
    * The JNDI Naming Context
    */
   private static Context namingContext;

   /**
    * The EJB 3.x remote business view of the EncryptionEJB
    */
   private static EncryptionRemoteBusiness encryptionRemoteBusiness;

   /**
    * JNDI Name of the Remote Business Reference
    */
   //TODO Use Global JNDI Syntax (not yet supported in JBoss EJB3)
   private static final String JNDI_NAME_ENCRYPTION_REMOTE_BUSINESS = EncryptionBean.EJB_NAME + "/remote";

   /**
    * Correlates to the env-entry within ejb-jar.xml, to be used as an override from the default 
    */
   private static final String EXPECTED_CIPHERS_PASSPHRASE = "OverriddenPassword";

   /**
    * Correlates to the env-entry within ejb-jar.xml, to be used as an override from the default 
    */
   private static final String EXPECTED_ALGORITHM_MESSAGE_DIGEST = "SHA";

   // ---------------------------------------------------------------------------||
   // Lifecycle Methods ---------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   @BeforeClass
   public static void beforeClass() throws Throwable
   {
      // Create the naming context, using jndi.properties on the CP
      namingContext = new InitialContext();

      // Obtain EJB 3.x Business Reference
      encryptionRemoteBusiness = (EncryptionRemoteBusiness) namingContext.lookup(JNDI_NAME_ENCRYPTION_REMOTE_BUSINESS);
   }

   // ---------------------------------------------------------------------------||
   // Tests ---------------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * These tests will use the EJB set up in test initialization
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
      this.assertHashing(encryptionRemoteBusiness);
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
      this.assertEncryption(encryptionRemoteBusiness);
   }

   /**
    * Ensures that the hashing algorithm was overridden 
    * from the environment entry declared in ejb-jar.xml
    * 
    * @throws Throwable
    */
   @Test
   public void testMessageDigestAlgorithmOverride() throws Throwable
   {
      // Log
      log.info("testMessageDigestAlgorithmOverride");

      // Get the algorithm used
      final String algorithm = encryptionRemoteBusiness.getMessageDigestAlgorithm();
      log.info("Using MessageDigest algorithm: " + algorithm);

      // Ensure expected
      TestCase.assertEquals("MessageDigest algorithm should have been overridden from the environment entry",
            EXPECTED_ALGORITHM_MESSAGE_DIGEST, algorithm);
   }

   /**
    * Ensures that the cipher passphrase was overridden 
    * from the environment entry declared in ejb-jar.xml
    * 
    * @throws Throwable
    */
   @Test
   public void testCiphersPassphraseOverride() throws Throwable
   {
      // Log
      log.info("testCiphersPassphraseOverride");

      // Get the algorithm used
      final String passphrase = encryptionRemoteBusiness.getCiphersPassphrase();
      log.info("Using Encryption passphrase: " + passphrase);

      // Ensure expected
      TestCase.assertEquals("Encryption passphrase should have been overridden from the environment entry",
            EXPECTED_CIPHERS_PASSPHRASE, passphrase);
   }

}
