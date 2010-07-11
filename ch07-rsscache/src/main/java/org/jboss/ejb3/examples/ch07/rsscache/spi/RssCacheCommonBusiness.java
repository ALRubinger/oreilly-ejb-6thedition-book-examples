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
package org.jboss.ejb3.examples.ch07.rsscache.spi;

import java.net.URL;
import java.util.List;

/**
 * Common business interface for beans exposing a cached view of an RSS 
 * Feed (ie. the EJB Container)
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public interface RssCacheCommonBusiness
{
   // ---------------------------------------------------------------------------||
   // Contracts -----------------------------------------------------------------||
   // ---------------------------------------------------------------------------||

   /**
    * Returns all entries in the RSS Feed represented by {@link RssCacheCommonBusiness#getUrl()}.
    * This list will not support mutation and is read-only.
    */
   List<RssEntry> getEntries();

   /**
    * Returns the URL of the RSS Feed
    * 
    * @return
    */
   URL getUrl();

   /**
    * Flushes the cache and refreshes the entries from the feed
    */
   void refresh();

}
