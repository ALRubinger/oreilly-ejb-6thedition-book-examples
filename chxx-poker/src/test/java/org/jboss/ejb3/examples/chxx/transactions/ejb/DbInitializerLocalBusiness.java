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
package org.jboss.ejb3.examples.chxx.transactions.ejb;

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

   public static final long ACCOUNT_ALRUBINGERL_ID = 1L;

   public static final long USER_POKERGAME_ID = 2L;

   public static final String USER_POKERGAME_NAME = "The Poker Game System";

   public static final String USER_POKERGAME_EMAIL = "pokergame@fake.com";

   public static final long ACCOUNT_POKERGAME_ID = 2L;

   public static final BigDecimal INITIAL_ACCOUNT_BALANCE_ALR = new BigDecimal(500);

   public static final BigDecimal INITIAL_ACCOUNT_BALANCE_POKERGAME = new BigDecimal(0);

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Clears and repopulates the database with default test data
    * 
    * @throws Exception If an error occurred in refreshing with default data
    */
   void refreshWithDefaultData() throws Exception;
}
