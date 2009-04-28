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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.jboss.logging.Logger;

/**
 * EncryptionBeanBase
 * 
 * Base for bean implementation classes of the EncyrptionEJB, 
 * provides business logic for required contracts
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public class EncryptionBeanBase implements EncryptionCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EncryptionBeanBase.class);

   /**
    * Charset used for encoding/decoding Strings to/from byte representation
    */
   private static final String CHARSET = "UTF-8";

   /**
    * Default Algorithm used by the Digest for one-way hashing
    */
   private static final String DEFAULT_ALGORITHM_MESSAGE_DIGEST = "MD5";

   /**
    * Default Algorithm used by the Cipher Key for symmetric encryption
    */
   private static final String DEFAULT_ALGORITHM_CIPHER = "PBEWithMD5AndDES";

   /**
    * The default passphrase for symmetric encryption/decryption
    */
   private static final String DEFAULT_PASSPHRASE = "LocalTestingPassphrase";

   /**
    * The salt used in symmetric encryption/decryption
    */
   private static final byte[] DEFAULT_SALT_CIPHERS =
   {(byte) 0xB4, (byte) 0xA2, (byte) 0x43, (byte) 0x89, 0x3E, (byte) 0xC5, (byte) 0x78, (byte) 0x53};

   /**
    * Iteration count used for symmetric encryption/decryption
    */
   private static final int DEFAULT_ITERATION_COUNT_CIPHERS = 20;

   // ---------------------------------------------------------------------------||
   // Instance Members ----------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * The following members represent the internal
    * state of the Service.  Note how these are *not* leaked out
    * via the end-user API, and are hence part of "internal state"
    * and not "conversational state".
    */

   /**
    * Digest used for one-way hashing
    */
   private MessageDigest messageDigest;

   /**
    * Cipher used for symmetric encryption
    */
   private Cipher encryptionCipher;

   /**
    * Cipher used for symmetric decryption
    */
   private Cipher decryptionCipher;

   // ---------------------------------------------------------------------------||
   // Lifecycle -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Initializes this service before it may handle requests
    * 
    * @throws Exception If some unexpected error occurred
    * @throws IllegalStateException If one of the required ciphers was not available
    */
   public void initialize() throws Exception, IllegalStateException
   {
      /*
       * Symmetric Encryption
       */

      // Obtain parameters used in initializing the ciphers
      final String cipherAlgorithm = DEFAULT_ALGORITHM_CIPHER;
      final byte[] ciphersSalt = DEFAULT_SALT_CIPHERS;
      final int ciphersIterationCount = DEFAULT_ITERATION_COUNT_CIPHERS;
      final String ciphersPassphrase = this.getCiphersPassphrase();

      // Obtain key and param spec for the ciphers
      final KeySpec ciphersKeySpec = new PBEKeySpec(ciphersPassphrase.toCharArray(), ciphersSalt, ciphersIterationCount);
      final SecretKey ciphersKey = SecretKeyFactory.getInstance(cipherAlgorithm).generateSecret(ciphersKeySpec);
      final AlgorithmParameterSpec paramSpec = new PBEParameterSpec(ciphersSalt, ciphersIterationCount);

      // Create and init the ciphers
      this.encryptionCipher = Cipher.getInstance(ciphersKey.getAlgorithm());
      this.decryptionCipher = Cipher.getInstance(ciphersKey.getAlgorithm());
      encryptionCipher.init(Cipher.ENCRYPT_MODE, ciphersKey, paramSpec);
      decryptionCipher.init(Cipher.DECRYPT_MODE, ciphersKey, paramSpec);

      // Log
      log.info("Initialized encryption cipher: " + this.encryptionCipher);
      log.info("Initialized decryption cipher: " + this.decryptionCipher);

      /*
       * One-way Hashing
       */

      // Get the algorithm for the MessageDigest
      final String messageDigestAlgorithm = this.getMessageDigestAlgorithm();

      // Create the MessageDigest
      try
      {
         this.messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new RuntimeException("Could not obtain the " + MessageDigest.class.getSimpleName() + " for algorithm: "
               + messageDigestAlgorithm, e);
      }
      log.info("Initialized MessageDigest for one-way hashing: " + this.messageDigest);
   }

   // ---------------------------------------------------------------------------||
   // Required Implementations --------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#compare(java.lang.String, java.lang.String)
    */
   @Override
   public boolean compare(final String hash, final String input) throws IllegalArgumentException
   {
      // Precondition checks
      if (hash == null)
      {
         throw new IllegalArgumentException("hash is required.");
      }
      if (input == null)
      {
         throw new IllegalArgumentException("Input is required.");
      }

      // Get the hash of the supplied input
      final String hashOfInput = this.hash(input);

      // Determine whether equal
      final boolean equal = hash.equals(hashOfInput);

      // Return
      return equal;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#decrypt(java.lang.String)
    */
   @Override
   public String decrypt(final String input) throws IllegalArgumentException, IllegalStateException
   {
      // Get the cipher
      final Cipher cipher = this.decryptionCipher;
      if (cipher == null)
      {
         throw new IllegalStateException("Decyrption cipher not available, has this service been initialized?");
      }

      // Run the cipher
      byte[] resultBytes = null;;
      try
      {
         final byte[] inputBytes = this.stringToByteArray(input);
         resultBytes = cipher.doFinal(Base64.decodeBase64(inputBytes));
      }
      catch (final Throwable t)
      {
         throw new RuntimeException("Error in decryption", t);
      }
      final String result = this.byteArrayToString(resultBytes);

      // Log
      log.info("Decryption on \"" + input + "\": " + result);

      // Return
      return result;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#encrypt(java.lang.String)
    */
   @Override
   public String encrypt(final String input) throws IllegalArgumentException
   {
      // Get the cipher
      final Cipher cipher = this.encryptionCipher;
      if (cipher == null)
      {
         throw new IllegalStateException("Encyrption cipher not available, has this service been initialized?");
      }

      // Get bytes from the String
      byte[] inputBytes = this.stringToByteArray(input);

      // Run the cipher
      byte[] resultBytes = null;
      try
      {
         resultBytes = Base64.encodeBase64(cipher.doFinal(inputBytes));
      }
      catch (final Throwable t)
      {
         throw new RuntimeException("Error in encryption of: " + input, t);
      }

      // Log
      log.info("Encryption on \"" + input + "\": " + this.byteArrayToString(resultBytes));

      // Return
      final String result = this.byteArrayToString(resultBytes);
      return result;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#hash(java.lang.String)
    */
   @Override
   public String hash(final String input) throws IllegalArgumentException
   {
      // Precondition check
      if (input == null)
      {
         throw new IllegalArgumentException("Input is required.");
      }

      // Get bytes from the input
      byte[] inputBytes = this.stringToByteArray(input);

      // Obtain the MessageDigest
      final MessageDigest digest = this.getMessageDigest();

      // Update with our input, and obtain the hash, resetting the messageDigest
      digest.update(inputBytes, 0, inputBytes.length);
      final byte[] hashBytes = digest.digest();
      final byte[] encodedBytes = Base64.encodeBase64(hashBytes);

      // Get the input back in some readable format
      final String hash = this.byteArrayToString(encodedBytes);
      log.info("One-way hash of \"" + input + "\": " + hash);

      // Return
      return hash;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#getCiphersPassphrase()
    */
   @Override
   public String getCiphersPassphrase()
   {
      return DEFAULT_PASSPHRASE;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionCommonBusiness#getMessageDigestAlgorithm()
    */
   @Override
   public String getMessageDigestAlgorithm()
   {
      return DEFAULT_ALGORITHM_MESSAGE_DIGEST;
   }

   // ---------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Obtains the charset used in encoding/decoding Strings 
    * to/from byte representation
    * 
    * @return The charset
    */
   protected String getCharset()
   {
      return CHARSET;
   }

   /**
    * Returns a String representation of the specified byte array
    * using the charset from {@link EncryptionBeanBase#getCharset()}.  Wraps 
    * any {@link UnsupportedEncodingException} as a result of using an invalid
    * charset in a {@link RuntimeException}.
    * 
    * @param bytes
    * @return
    * @throws RuntimeException If the charset was invalid, or some otehr unknown error occurred 
    * @throws IllegalArgumentException If the byte array was not specified
    */
   protected final String byteArrayToString(final byte[] bytes) throws RuntimeException, IllegalArgumentException
   {
      // Precondition check
      if (bytes == null)
      {
         throw new IllegalArgumentException("Byte array is required.");
      }

      // Represent as a String
      String result = null;
      final String charset = this.getCharset();
      try
      {
         result = new String(bytes, charset);
      }
      catch (final UnsupportedEncodingException e)
      {
         throw new RuntimeException("Specified charset is invalid: " + charset, e);
      }

      // Return
      return result;
   }

   /**
    * Returns a byte array representation of the specified String
    * using the charset from {@link EncryptionBeanBase#getCharset()}.  Wraps 
    * any {@link UnsupportedEncodingException} as a result of using an invalid
    * charset in a {@link RuntimeException}.
    * 
    * @param input
    * @return
    * @throws RuntimeException If the charset was invalid, or some otehr unknown error occurred 
    * @throws IllegalArgumentException If the input was not specified (null)
    */
   protected final byte[] stringToByteArray(final String input) throws RuntimeException, IllegalArgumentException
   {
      // Precondition check
      if (input == null)
      {
         throw new IllegalArgumentException("Input is required.");
      }

      // Represent as a String
      byte[] result = null;
      final String charset = this.getCharset();
      try
      {
         result = input.getBytes(charset);
      }
      catch (final UnsupportedEncodingException e)
      {
         throw new RuntimeException("Specified charset is invalid: " + charset, e);
      }

      // Return
      return result;
   }

   /**
    * Returns the MessageDigest to be used for hashing
    * 
    * @return
    * @throws IllegalStateException If the messageDigest has not yet been initialized
    */
   protected final MessageDigest getMessageDigest() throws IllegalStateException
   {
      // Get
      final MessageDigest digest = this.messageDigest;

      // Ensure init'd
      if (digest == null)
      {
         throw new IllegalStateException(MessageDigest.class.getSimpleName() + " has not yet been initialized");
      }

      // Return
      return digest;
   }
}
