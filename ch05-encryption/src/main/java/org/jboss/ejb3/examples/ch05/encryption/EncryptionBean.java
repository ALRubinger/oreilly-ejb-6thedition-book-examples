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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.logging.Logger;

/**
 * EncryptionBean
 * 
 * Bean implementation class of the EncryptionEJB.  Shows
 * how lifecycle callbacks are implemented (@PostConstruct),
 * and two ways of obtaining externalized environment
 * entries. 
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateless(name = EncryptionBean.EJB_NAME)
@Local(EncryptionLocalBusiness.class)
@Remote(EncryptionRemoteBusiness.class)
public class EncryptionBean extends EncryptionBeanBase implements EncryptionLocalBusiness, EncryptionRemoteBusiness
{
   // ---------------------------------------------------------------------------||
   // Class Members -------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(EncryptionBean.class);

   /**
    * Name we'll assign to this EJB, will be referenced in the corresponding 
    * META-INF/ejb-jar.xml file
    */
   static final String EJB_NAME = "EncryptionEJB";

   /**
    * Name of the environment entry representing the ciphers' passphrase supplied
    * in ejb-jar.xml
    */
   private static final String ENV_ENTRY_NAME_CIPHERS_PASSPHRASE = "ciphersPassphrase";

   // ---------------------------------------------------------------------------||
   // Instance Members ----------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * SessionContext of this EJB; this will be injected by the EJB 
    * Container as it's marked w/ @Resource
    */
   @Resource
   private SessionContext context;

   /**
    * Passphrase to use  for the key in cipher operations; lazily initialized
    * and loaded via SessionContext.lookup
    */
   private String ciphersPassphrase;

   //TODO https://jira.jboss.org/jira/browse/EJBTHREE-1813
   //TODO https://jira.jboss.org/jira/browse/EJBBOOK-5
   /*
    * injection-target in XML should *NOT* be required here.
    */
   /**
    * Algorithm to use in message digest (hash) operations, injected
    * via @Resource annotation
    */
   @Resource
   private String messageDigestAlgorithm;

   // ---------------------------------------------------------------------------||
   // Lifecycle -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||
   /**
    * Here we extend the implementation 
    * of {@link EncryptionBeanBase#initialize()} to:
    * 
    * 1) Apply the @PostConstruct annotation such that the method
    *       is fired by the EJB Container as part of the SLSB lifecycle
    * 2) Provide some logging to show when its called
    */

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionBeanBase#initialize()
    */
   @Override
   @PostConstruct
   public void initialize() throws Exception
   {
      // Log that we're here
      log.info("Initializing, part of " + PostConstruct.class.getName() + " lifecycle");

      // Call super implementation
      super.initialize();
   }

   /*
    * We'll override the methods that return the some configuration
    * so that we may externalize these values by way of EJB Environment Entries.
    * Later we may look these up via a SessionContext that the Container will
    * supply to this SLSB Bean instance (@see the @Resource annotation above
    * the SessionContext member above)
    */

   /**
    * Override the way we get the ciphers' passphrase so that we may 
    * define it in a secure location on the server.  Now our production
    * systems will use a different key for encoding than our development
    * servers, and we may limit the likelihood of a security breach 
    * while still allowing our programmer to use the default passphrase
    * transparently during development.  
    * 
    * If not provided as an env-entry, fall back upon the default.
    * 
    * Note that a real system won't expose this method in the public API, ever.  We
    * do here for testing and to illustrate the example.
    * 
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionBeanBase#getCiphersPassphrase()
    */
   @Override
   public String getCiphersPassphrase()
   {
      // Obtain current
      String passphrase = this.ciphersPassphrase;

      // If not set
      if (passphrase == null)
      {

         // Do a lookup via SessionContext
         passphrase = this.getEnvironmentEntryAsString(ENV_ENTRY_NAME_CIPHERS_PASSPHRASE);

         // See if provided
         if (passphrase == null)
         {

            // Log a warning
            log.warn("No encryption passphrase has been supplied explicitly via "
                  + "an env-entry, falling back on the default...");

            // Set
            passphrase = super.getCiphersPassphrase();
         }

         // Set the passphrase to be used so we don't have to do this lazy init again
         this.ciphersPassphrase = passphrase;
      }

      // In a secure system, we don't log this. ;)
      log.info("Using encryption passphrase for ciphers keys: " + passphrase);

      // Return 
      return passphrase;
   }

   /**
    * Obtains the message digest algorithm as injected from the env-entry element
    * defined in ejb-jar.xml.  If not specified, fall back onto the default, logging a warn 
    * message
    * 
    * @see org.jboss.ejb3.examples.ch05.encryption.EncryptionRemoteBusiness#getMessageDigestAlgorithm()
    */
   @Override
   public String getMessageDigestAlgorithm()
   {
      // First see if this has been injected/set
      if (this.messageDigestAlgorithm == null)
      {
         // Log a warning
         log.warn("No message digest algorithm has been supplied explicitly via "
               + "an env-entry, falling back on the default...");

         // Set
         this.messageDigestAlgorithm = super.getMessageDigestAlgorithm();
      }

      // Log
      log.info("Configured MessageDigest one-way hash algorithm is: " + this.messageDigestAlgorithm);

      // Return
      return this.messageDigestAlgorithm;
   }

   // ---------------------------------------------------------------------------||
   // Internal Helper Methods ---------------------------------------------------||
   // ---------------------------------------------------------------------------||
   /**
    * Obtains the environment entry with the specified name, casting to a String,
    * and returning the result.  If the entry is not assignable 
    * to a String, an {@link IllegalStateException} will be raised.  In the event that the 
    * specified environment entry cannot be found, a warning message will be logged
    * and we'll return null.
    * 
    * @param envEntryName
    * @return
    * @throws IllegalStateException
    */
   private String getEnvironmentEntryAsString(final String envEntryName) throws IllegalStateException
   {
      // Lookup in the Private JNDI ENC via the injected SessionContext
      Object lookupValue = null;
      try
      {
         lookupValue = this.context.lookup(envEntryName);
         log.debug("Obtained environment entry \"" + envEntryName + "\": " + lookupValue);
      }
      catch (final IllegalArgumentException iae)
      {
         // Not found defined within this EJB's Component Environment, 
         // so return null and let the caller handle it
         log.warn("Could not find environment entry with name: " + envEntryName);
         return null;
      }

      // Cast
      String returnValue = null;
      try
      {
         returnValue = String.class.cast(lookupValue);
      }
      catch (final ClassCastException cce)
      {
         throw new IllegalStateException("The specified environment entry, " + lookupValue
               + ", was not able to be represented as a " + String.class.getName(), cce);
      }

      // Return
      return returnValue;
   }

}
