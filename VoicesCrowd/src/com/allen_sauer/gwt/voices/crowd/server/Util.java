/*
 * Copyright 2010 Fred Sauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.crowd.server;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import com.allen_sauer.gwt.voices.crowd.shared.TestResultSummary;
import com.allen_sauer.gwt.voices.crowd.shared.TestResults;
import com.allen_sauer.gwt.voices.crowd.shared.UserAgent;
import com.allen_sauer.gwt.voices.crowd.shared.UserAgentSummary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class Util {

  private static final int BUFFER_SIZE = 4096;

  public static void incrementTestResultCount(
      UserAgent userAgent, String gwtUserAgent, TestResults testResults) throws IOException {
    UserAgentSummary userAgentSummary = lookupPrettyUserAgent(userAgent.toString(), gwtUserAgent);
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery(TestResultSummary.class);
      query.setFilter(
          "userAgent == userAgentParam && gwtUserAgent == gwtUserAgentParam && results == resultsParam");
      query.declareParameters(
          "String userAgentParam, String gwtUserAgentParam, String resultsParam");
      List<TestResultSummary> summaryList = (List<TestResultSummary>) query.execute(
          userAgent.toString(), gwtUserAgent, testResults.toString());
      TestResultSummary summary;

      if (summaryList.isEmpty()) {
        summary = new TestResultSummary(
            userAgent, userAgentSummary.getPrettyUserAgent(), gwtUserAgent, testResults);
      } else {
        summary = summaryList.get(0);

        if (summaryList.size() > 1) {
          // merge rows in case race condition caused > 1 row to be inserted
          TestResultSummary anotherSummary = summaryList.get(1);
          summary.incrementCount(anotherSummary.getCount());
          pm.deletePersistent(anotherSummary);
        }

        // count the current test results
        summary.incrementCount(1);

        if (summary.getPrettyUserAgent() == null) {
          summary.setPrettyUserAgent(
              lookupPrettyUserAgent(userAgent.toString(), gwtUserAgent).getPrettyUserAgent());
        }
      }
      pm.makePersistent(summary);
    } finally {
      pm.close();
    }
  }

  public static UserAgentSummary lookupPrettyUserAgent(String userAgentString, String gwtUserAgent)
      throws IOException {
    MemcacheService mc = MemcacheServiceFactory.getMemcacheService();

    // Get info out of memcache
    UserAgentSummary userAgentSummary = (UserAgentSummary) mc.get(userAgentString);
    if (userAgentSummary != null) {
      return userAgentSummary;
    } else {

      // Next, try the datastore
      userAgentSummary = lookupUserAgentInDatastore(userAgentString, userAgentSummary);

      // Next, browserscope it
      if (userAgentSummary == null) {
        userAgentSummary = lookupUserAgentInBrowserScope(userAgentString, gwtUserAgent);
      }

      // Store result in memcache
      if (userAgentSummary != null) {
        persistUserAgentSummary(userAgentSummary);
        mc.put(userAgentString, userAgentSummary);
      }
    }
    return userAgentSummary;
  }

  private static void persistUserAgentSummary(UserAgentSummary userAgentSummary) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(userAgentSummary);
    } finally {
      pm.close();
    }
  }

  public static UserAgentSummary lookupUserAgentInDatastore(
      String userAgentString, UserAgentSummary userAgentSummary) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(UserAgentSummary.class, "userAgentString == userAgentStringParam");
    query.declareParameters("String userAgentStringParam");
    List<UserAgentSummary> uaList = (List<UserAgentSummary>) query.execute(userAgentString);
    if (!uaList.isEmpty()) {
      userAgentSummary = uaList.get(0);
    }
    return userAgentSummary;
  }

  public static UserAgentSummary lookupUserAgentInBrowserScope(
      String userAgentString, String gwtUserAgent)
      throws MalformedURLException, IOException, UnsupportedEncodingException {
    URL url = new URL("http://www.browserscope.org/");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("User-Agent", userAgentString);

    ByteArrayInputStream content = (ByteArrayInputStream) connection.getContent();

    byte[] buffer = new byte[BUFFER_SIZE];
    ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
    try {
      while (true) {
        int byteCount = content.read(buffer);
        if (byteCount == -1) {
          break;
        }
        out.write(buffer, 0, byteCount);
      }
      String pageContent = out.toString("UTF-8");
      Pattern pattern = Pattern.compile("var userAgentPretty = '(.*?)';");
      Matcher matcher = pattern.matcher(pageContent);
      if (matcher.find()) {
        String prettyUserAgent = matcher.group(1);
        return new UserAgentSummary(userAgentString, prettyUserAgent, gwtUserAgent);
      } else {
        return null;
      }
    } finally {
      if (content != null) {
        content.close();
      }
    }
  }

}
