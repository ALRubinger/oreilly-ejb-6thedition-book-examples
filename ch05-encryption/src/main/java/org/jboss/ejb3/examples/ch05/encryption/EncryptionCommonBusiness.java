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

import java.util.concurrent.Future;

/**
 * Contains the contract for operations common to 
 * all business interfaces of the EncryptionEJB
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 */
public interface EncryptionCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Encrypts the specified String, returning the result  
    * 
    * @param input
    * @return
    * @throws IllegalArgumentException If no input was provided (null)
    * @throws EncryptionException If some problem occurred with encryption
    */
   String encrypt(String input) throws IllegalArgumentException, EncryptionException;

   /**
    * Decrypts the specified String, returning the result.  The general
    * contract is that the result of decrypting a String encrypted with
    * {@link EncryptionCommonBusiness#encrypt(String)} will be equal 
    * by value to the original input (round trip).
    * 
    * @param input
    * @return
    * @throws IllegalArgumentException If no input was provided (null)
    * @throws EncryptionException If some problem occurred with decryption
    */
   String decrypt(String input) throws IllegalArgumentException, EncryptionException;

   /**
    * Returns a one-way hash of the specified argument.  Useful
    * for safely storing passwords.
    * 
    * @param input
    * @return
    * @throws IllegalArgumentException If no input was provided (null)
    * @throws EncryptionException If some problem occurred making the hash
    */
   String hash(String input) throws IllegalArgumentException, EncryptionException;
   
   /**
    * Returns a one-way hash of the specified argument, calculated asynchronously.  
    * Useful for safely storing passwords.
    * 
    * @param input
    * @return
    * @throws IllegalArgumentException
    * @throws EncryptionException
    */
   Future<String> hashAsync(String input) throws IllegalArgumentException, EncryptionException;

   /**
    * Returns whether or not the specified input matches the specified 
    * hash.  Useful for validating passwords against a 
    * securely-stored hash. 
    * 
    * @param hash
    * @param input
    * @return
    * @throws IllegalArgumentException If either the hash or input is not provided (null)
    * @throws EncryptionException If some problem occurred making the hash
    */
   boolean compare(String hash, String input) throws IllegalArgumentException, EncryptionException;

   /*
    * This comment applies to all below this marker.
    * 
    * In real life it's a security risk to expose these internals, 
    * but they're in place here for testing and to show 
    * functionality described by the examples.
    */

   /**
    * Obtains the passphrase to be used in the key for
    * the symmetric encryption/decryption ciphers
    * 
    * @return
    */
   String getCiphersPassphrase();

   /**
    * Obtains the algorithm to be used in performing
    * one-way hashing
    * 
    * @return
    */
   String getMessageDigestAlgorithm();

}
