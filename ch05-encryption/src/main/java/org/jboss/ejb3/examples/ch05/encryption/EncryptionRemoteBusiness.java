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

/**
 * EncryptionRemoteBusiness
 * 
 * EJB 3.x Remote Business View of the EncryptionEJB
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface EncryptionRemoteBusiness extends EncryptionCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * Here we add some support in addition to methods defined by EncryptionCommonBusiness
    * which will be specific to our EJB.  In real life it's a security risk to 
    * expose these internals, but they're in place here for testing and to show 
    * functionality described by the examples
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
