package org.jboss.ejb3.examples.ch07.rsscache.impl.rome;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness;
import org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HttpClientFeedFetcher;
import com.sun.syndication.io.FeedException;

/**
 * Singleton EJB, to be eagerly instantiated upon application deployment,
 * exposing a cached view of an RSS Feed
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
@Singleton
@Startup
@Remote(RssCacheCommonBusiness.class)
// Explicitly declare Container Managed Concurrency, which is unnecessary; it's the default
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class RssCacheBean implements RssCacheCommonBusiness
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(RssCacheBean.class.getName());

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * URL pointing to the RSS Feed
    */
   private URL url;

   /**
    * Cached RSS Entries for the feed
    */
   private List<RssEntry> entries;

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness#getEntries()
    */
   @Override
   @Lock(LockType.READ)
   public List<RssEntry> getEntries()
   {
      return entries;
   }

   /* (non-Javadoc)
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness#getUrl()
    */
   @Lock(LockType.READ)
   @Override
   public URL getUrl()
   {
      // Return a copy so we don't export mutable state to the client
      return ProtectExportUtil.copyUrl(this.url);
   }

   /**
    * @see org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness#refresh()
    * @throws IllegalStateException If the URL has not been set
    */
   @PostConstruct
   @Override
   // Block all readers and writers until we're done here; Optional metadata, WRITE is the default
   @Lock(LockType.WRITE)
   public void refresh() throws IllegalStateException
   {

      // Obtain the URL
      final URL url = this.url;
      if (url == null)
      {
         throw new IllegalStateException("The Feed URL has not been set");
      }
      log.info("Requested: " + url);

      // Obtain the feed
      final FeedFetcher feedFetcher = new HttpClientFeedFetcher();
      SyndFeed feed = null;
      try
      {
         feed = feedFetcher.retrieveFeed(url);
      }
      catch (final FeedException fe)
      {
         throw new RuntimeException(fe);
      }
      catch (final FetcherException fe)
      {
         throw new RuntimeException(fe);
      }
      catch (final IOException ioe)
      {
         throw new RuntimeException(ioe);
      }

      // Make a new list for the entries
      final List<RssEntry> rssEntries = new ArrayList<RssEntry>();

      // For each entry
      @SuppressWarnings("unchecked")
      // The Rome API doesn't provide for generics, so suppress the warning
      final List<SyndEntry> list = (List<SyndEntry>) feed.getEntries();
      for (final SyndEntry entry : list)
      {
         // Make a new entry
         final RssEntry rssEntry = new RomeRssEntry(entry);

         // Place in the list
         rssEntries.add(rssEntry);
         log.fine("Found new RSS Entry: " + rssEntry);
      }

      // Protect the entries from mutation from exporting the client view
      final List<RssEntry> protectedEntries = Collections.unmodifiableList(rssEntries);

      // Set the entries in the cache
      this.entries = protectedEntries;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Sets the URL pointing to the feed
    * 
    * @param url
    * @throws IllegalArgumentException If the URL is null
    */
   void setUrl(final URL url) throws IllegalArgumentException
   {
      // Set the URL
      this.url = url;

      // Refresh
      this.refresh();
   }
}
