/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.examples.chxx.transactions;

import java.math.BigDecimal;

/**
 * Contract of an EJB which can reset and populate a database with
 * known data for user tests
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
public interface DbInitializerLocalBusiness
{
   //-------------------------------------------------------------------------------------||
   // Constants --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   
   /**
    * Name we'll ind to in JNDI
    */
   String JNDI_NAME = "DbInitializer";

   /*
    * Test Data
    */

   public static final long USER_ALRUBINGER_ID = 1L;

   public static final String USER_ALRUBINGER_NAME = "Andrew Lee Rubinger";

   public static final String USER_ALRUBINGER_EMAIL = "alr@fake.com";

   public static final long ACCOUNT_ALRUBINGER_PERSONAL_ID = 1L;

   public static final long ACCOUNT_ALRUBINGER_POKER_ID = 2L;

   public static final long USER_DERUDMAN_ID = 2L;

   public static final String USER_DERUDMAN_NAME = "David Edward Rudman";

   public static final String USER_DERUDMAN_EMAIL = "der@fake.com";

   public static final long ACCOUNT_DERUDMAN_PERSONAL_ID = 3L;

   public static final long ACCOUNT_DERUDMAN_POKER_ID = 4L;

   public static final BigDecimal INITIAL_PERSONAL_ACCOUNT_BALANCE_ALR = new BigDecimal(500);

   public static final BigDecimal INITIAL_PERSONAL_ACCOUNT_BALANCE_DER = new BigDecimal(1000);

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Clears and repopulates the database with default test data
    */
   void refreshWithDefaultData();
}
