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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.ejb.EJB;

import junit.framework.TestCase;

import org.apache.commons.codec.BinaryEncoder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the EncryptionEJB
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@RunWith(Arquillian.class)
public class EncryptionIntegrationTestCase extends EncryptionTestCaseSupport
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EncryptionIntegrationTestCase.class.getName());

   /**
    * The EJB 3.x local business view of the EncryptionEJB
    */
   @EJB(mappedName="java:module/EncryptionEJB!org.jboss.ejb3.examples.ch05.encryption.EncryptionLocalBusiness")
   private EncryptionLocalBusiness encryptionLocalBusiness;

   /**
    * Correlates to the env-entry within ejb-jar.xml, to be used as an override from the default 
    */
   private static final String EXPECTED_CIPHERS_PASSPHRASE = "OverriddenPassword";

   /**
    * Correlates to the env-entry within ejb-jar.xml, to be used as an override from the default 
    */
   private static final String EXPECTED_ALGORITHM_MESSAGE_DIGEST = "SHA";

   /**
    * Define the deployment
    */
   @Deployment
   public static JavaArchive createDeployment() throws MalformedURLException
   {
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "slsb.jar").addClasses(EncryptionBean.class,
            EncryptionCommonBusiness.class, EncryptionLocalBusiness.class, EncryptionRemoteBusiness.class,
            EncryptionException.class, EncryptionTestCaseSupport.class).addAsManifestResource(
            new URL(EncryptionIntegrationTestCase.class.getProtectionDomain().getCodeSource().getLocation(),
                  "../classes/META-INF/ejb-jar.xml"), "ejb-jar.xml").addPackages(true,BinaryEncoder.class.getPackage());
      //TODO SHRINKWRAP-141 Make addition of the ejb-jar less verbose
      log.info(archive.toString(true));
      return archive;
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
      this.assertHashing(encryptionLocalBusiness);
   }
   
   /**
    * Ensures that hashing works when used asynchronously
    * @throws Exception
    */
   @Test
   public void testAsyncHashing() throws Exception
   {
      // Log
      log.info("testAsyncHashing");

      // Declare the input
      final String input = "Async Hashing Input";

      // Hash
      final Future<String> hashFuture = encryptionLocalBusiness.hashAsync(input);
      final String hash = hashFuture.get(10,TimeUnit.SECONDS);
      log.info("Hash of \"" + input + "\": " + hash);

      // Test that the has function had some effect
      TestCase.assertNotSame("The hash function had no effect upon the supplied input", input, hash);

      // Get the comparison result
      final boolean equal = encryptionLocalBusiness.compare(hash, input);

      // Test that the input matches the hash we'd gotten
      TestCase.assertTrue("The comparison of the input to its hashed result failed", equal);
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
      this.assertEncryption(encryptionLocalBusiness);
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
      final String algorithm = encryptionLocalBusiness.getMessageDigestAlgorithm();
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
      final String passphrase = encryptionLocalBusiness.getCiphersPassphrase();
      log.info("Using Encryption passphrase: " + passphrase);

      // Ensure expected
      TestCase.assertEquals("Encryption passphrase should have been overridden from the environment entry",
            EXPECTED_CIPHERS_PASSPHRASE, passphrase);
   }

}
