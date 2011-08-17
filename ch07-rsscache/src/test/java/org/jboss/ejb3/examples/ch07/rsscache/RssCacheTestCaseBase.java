package org.jboss.ejb3.examples.ch07.rsscache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.jboss.ejb3.examples.ch07.rsscache.spi.RssCacheCommonBusiness;
import org.jboss.ejb3.examples.ch07.rsscache.spi.RssEntry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 * Base tests for the RssCache @Singleton 
 * test classes, may be extended either from unit or 
 * integration tests.
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 */
public abstract class RssCacheTestCaseBase
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static final Logger log = Logger.getLogger(RssCacheTestCaseBase.class.getName());

   /**
    * The number of expected RSS entries from the default RSS Feed
    */
   private static final int EXPECTED_15_RSS_ENTRIES = 15;

   /**
    * The number of expected RSS entries from the RSS Feed with 5 entries
    */
   private static final int EXPECTED_5_RSS_ENTRIES = 5;

   /**
    * Filename containing a mock RSS feed for use in testing
    */
   static final String FILENAME_RSS_MOCK_FEED_15_ENTRIES = "15_entries.rss";

   /**
    * Filename containing a mock RSS feed for use in testing
    */
   static final String FILENAME_RSS_MOCK_FEED_5_ENTRIES = "5_entries.rss";

   /**
    * Filename of the target RSS feed to be requested of the HTTP server
    */
   static final String FILENAME_RSS_FEED = "feed.rss";

   /**
    * Port to which the test HTTP Server should bind
    */
   static final int HTTP_TEST_BIND_PORT = 12345;

   /**
    * Content type of an RSS feed 
    */
   private static final String CONTENT_TYPE_RSS = "text/rss";

   /**
    * The HTTP Server used to serve out the mock RSS file
    */
   static Server httpServer;

   /**
    * Newline character
    */
   private static final char NEWLINE = '\n';

   //-------------------------------------------------------------------------------------||
   // Lifecycle --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Starts up an embedded HTTP Server to serve out the Mock 
    * RSS file (Rome FeedFetcher doesn't support obtaining from 
    * file:/ URLs)
    */
   @BeforeClass
   public static void startHttpServer()
   {
      // Start an Embedded HTTP Server
      final Handler handler = new StaticFileHandler();
      final Server httpServer = new Server(HTTP_TEST_BIND_PORT);
      httpServer.setHandler(handler);
      try
      {
         httpServer.start();
      }
      catch (final Exception e)
      {
         throw new RuntimeException("Could not start server");
      }
      log.info("HTTP Server Started: " + httpServer);
      RssCacheUnitTestCase.httpServer = httpServer;
   }

   /**
    * Creates the RSS feed file from the default mock template
    */
   @BeforeClass
   public static void createRssFeedFile() throws Exception
   {
      writeToRssFeedFile(getMock15EntriesRssFile());
   }

   /**
    * Shuts down and clears the Embedded HTTP Server
    */
   @AfterClass
   public static void shutdownHttpServer()
   {
      if (httpServer != null)
      {
         try
         {
            httpServer.stop();
         }
         catch (final Exception e)
         {
            // Swallow
            log.severe("Could not stop HTTP Server cleanly: " + e.getMessage());
         }
         log.info("HTTP Server Stopped: " + httpServer);
         httpServer = null;
      }
   }

   /**
    * Removes the RSS feed file 
    */
   @AfterClass
   public static void deleteRssFeedFile() throws Exception
   {
      final File rssFile = getRssFeedFile();
      boolean deleted = rssFile.delete();
      if (!deleted)
      {
         log.warning("RSS Feed File was not cleaned up properly: " + rssFile);
      }
   }

   //-------------------------------------------------------------------------------------||
   // Tests ------------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures the RSS Entries have been initialized and parsed 
    * out as expected.  Additionally tests that {@link RssCacheCommonBusiness#refresh()}
    * clears the cache and works as expected
    */
   @Test
   public void testRssEntries() throws Exception
   {
      // Log
      log.info("testRssEntries");

      // Get the RSS Cache Bean
      final RssCacheCommonBusiness rssCache = this.getRssCacheBean();

      // Get all entries
      final List<RssEntry> rssEntries = rssCache.getEntries();
      log.info("Got entries: " + rssEntries);

      // Ensure they've been specified/initialized, and parsed out in proper size
      this.ensureExpectedEntries(rssEntries, EXPECTED_15_RSS_ENTRIES);

      // Swap out the contents of the RSS Feed File, so a refresh will pull in the new contents
      writeToRssFeedFile(getMock5EntriesRssFile());

      // Refresh 
      rssCache.refresh();

      // Get all entries
      final List<RssEntry> rssEntriesAfterRefresh = rssCache.getEntries();
      log.info("Got entries after refresh: " + rssEntriesAfterRefresh);

      // Ensure they've been specified/initialized, and parsed out in proper size
      this.ensureExpectedEntries(rssEntriesAfterRefresh, EXPECTED_5_RSS_ENTRIES);

      // Now put back the original 15 mock entries
      writeToRssFeedFile(getMock15EntriesRssFile());
      rssCache.refresh();

      // And ensure we're back to normal
      final List<RssEntry> rssEntriesAfterRestored = rssCache.getEntries();
      log.info("Got entries: " + rssEntriesAfterRestored);
      this.ensureExpectedEntries(rssEntriesAfterRestored, EXPECTED_15_RSS_ENTRIES);

   }

   //-------------------------------------------------------------------------------------||
   // Contracts --------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Obtains the RssCache bean to be used for this test
    */
   protected abstract RssCacheCommonBusiness getRssCacheBean();

   //-------------------------------------------------------------------------------------||
   // Internal Helper Methods ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Ensures that the RSS entries are parsed out and have the specified number of elements
    * @param entries
    * @param expectedSize
    */
   private void ensureExpectedEntries(final List<RssEntry> entries, final int expectedSize)
   {
      // Ensure they've been specified/initialized, and parsed out in proper size
      Assert.assertNotNull("RSS Entries was either not initialized or is returning null", entries);
      final int actualSize = entries.size();
      Assert.assertEquals("Wrong number of RSS entries parsed out from feed", expectedSize, actualSize);
      log.info("Got expected " + expectedSize + " RSS entries");
   }

   /**
    * Obtains the base of the code source
    */
   private static URL getCodebaseLocation()
   {
      return RssCacheUnitTestCase.class.getProtectionDomain().getCodeSource().getLocation();
   }

   /**
    * Writes the contents of the template file to the RSS Feed File
    * 
    * @param templateFile
    * @throws Exception
    */
   private static void writeToRssFeedFile(final File templateFile) throws Exception
   {
      // Get a writer to the target file
      final File rssFile = getRssFeedFile();
      final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(rssFile)));

      // Get a reader to the default mock template file
      final BufferedReader reader = new BufferedReader(new FileReader(templateFile));

      // Read 'n Write
      String line = null;
      while ((line = reader.readLine()) != null)
      {
         writer.write(line);
         writer.write(NEWLINE);
      }

      // Flush and close
      writer.flush();
      writer.close();
      reader.close();
   }

   /**
    * Obtains the RSS Feed file served by the server
    * @return
    * @throws Exception
    */
   private static File getRssFeedFile() throws Exception
   {
      final File baseFile = getBaseDirectory();
      final File rssFile = new File(baseFile, FILENAME_RSS_FEED);
      return rssFile;
   }

   /**
    * Obtains the Mock RSS Template file with 15 entries
    * @return
    * @throws Exception
    */
   private static File getMock15EntriesRssFile() throws Exception
   {
      return getFileFromBase(FILENAME_RSS_MOCK_FEED_15_ENTRIES);
   }

   /**
    * Obtains the Mock RSS Template file with 5 entries
    * @return
    * @throws Exception
    */
   private static File getMock5EntriesRssFile() throws Exception
   {
      return getFileFromBase(FILENAME_RSS_MOCK_FEED_5_ENTRIES);
   }

   /**
    * Obtains the file with the specified name from the base directory
    * 
    * @param filename
    * @return
    * @throws Exception
    */
   private static File getFileFromBase(final String filename) throws Exception
   {
      final File baseFile = getBaseDirectory();
      final File mockTemplateFile = new File(baseFile, filename);
      return mockTemplateFile;
   }

   /**
    * Obtains the base directory in which test files are located
    * @return
    */
   private static File getBaseDirectory() throws Exception
   {
      final URL baseLocation = getCodebaseLocation();
      final URI baseUri = baseLocation.toURI();
      final File baseFile = new File(baseUri);
      return baseFile;
   }

   //-------------------------------------------------------------------------------------||
   // Inner Classes ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Jetty Handler to serve a static character file from the web root
    */
   private static class StaticFileHandler extends AbstractHandler implements Handler
   {
      /*
       * (non-Javadoc)
       * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
       */
      public void handle(final String target, final HttpServletRequest request, final HttpServletResponse response,
            final int dispatch) throws IOException, ServletException
      {
         // Set content type and status before we write anything to the stream
         response.setContentType(CONTENT_TYPE_RSS);
         response.setStatus(HttpServletResponse.SC_OK);

         // Obtain the requested file relative to the webroot
         final URL root = getCodebaseLocation();
         final URL fileUrl = new URL(root.toExternalForm() + target);
         URI uri = null;
         try
         {
            uri = fileUrl.toURI();
         }
         catch (final URISyntaxException urise)
         {
            throw new RuntimeException(urise);
         }
         final File file = new File(uri);

         // File not found, so 404
         if (!file.exists())
         {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.log(Level.WARNING, "Requested file is not found: " + file);
            return;
         }

         // Write out each line
         final BufferedReader reader = new BufferedReader(new FileReader(file));
         final PrintWriter writer = response.getWriter();
         String line = null;
         while ((line = reader.readLine()) != null)
         {
            writer.println(line);
         }

         // Close 'er up
         writer.flush();
         reader.close();
         writer.close();
      }
   }

}
