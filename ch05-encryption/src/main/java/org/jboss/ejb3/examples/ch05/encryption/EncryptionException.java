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

import javax.ejb.ApplicationException;

/**
 * A checked Application Exception denoting
 * some unexpected problem with Encryption operations
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 */
@ApplicationException
// Explicit annotation, though this is inferred as default because we extend Exception
public class EncryptionException extends Exception
{

   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * To satisfy explicit serialization hints to the JVM
    */
   private static final long serialVersionUID = 1L;

   // ---------------------------------------------------------------------------||
   // Constructors --------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /*
    * All constructors will delegate to the superclass implementation
    */

   public EncryptionException()
   {
      super();
   }

   public EncryptionException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public EncryptionException(String message)
   {
      super(message);
   }

   public EncryptionException(Throwable cause)
   {
      super(cause);
   }

}
