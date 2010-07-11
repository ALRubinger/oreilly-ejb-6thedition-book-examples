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
package org.jboss.ejb3.examples.ch07.rsscache.impl.rome;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

/**
 * The java.net Rome implementation of an RSS Entry
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public class RomeRssEntry implements RssEntry
{

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * The author of the entry
    */
   private String author;

   /**
    * The short description of the entry
    */
   private String description;

   /**
    * The title of the entry
    */
   private String title;

   /**
    * The link to the entry
    */
   private URL url;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Constructor
    * 
    * @param entry The Rome API's RSS Entry representation
    * @throws IllegalArgumentException If the entry is not specified 
    */
   RomeRssEntry(final SyndEntry entry) throws IllegalArgumentException
   {
      // Set properties
      this.author = entry.getAuthor();
      final SyndContent content = entry.getDescription();
      this.description = content.getValue();
      this.title = entry.getTitle();
      final String urlString = entry.getLink();
      URL url = null;
      try
      {
         url = new URL(urlString);
      }
      catch (final MalformedURLException murle)
      {
         throw new RuntimeException("Obtained invalid URL from Rome RSS entry: " + entry, murle);
      }
      this.url = url;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry#getAuthor()
    */
   @Override
   public String getAuthor()
   {
      return this.author;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry#getDescription()
    */
   @Override
   public String getDescription()
   {
      return this.description;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry#getTitle()
    */
   @Override
   public String getTitle()
   {
      return this.title;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry#getUrl()
    */
   @Override
   public URL getUrl()
   {
      return ProtectExportUtil.copyUrl(this.url);
   }

   //-------------------------------------------------------------------------------------||
   // Overridden Implementations ---------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(this.getTitle());
      sb.append(" - ");
      sb.append(this.url.toExternalForm());
      return sb.toString();
   }
}
