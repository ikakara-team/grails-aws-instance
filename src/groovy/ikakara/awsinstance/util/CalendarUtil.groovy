/* Copyright 2014-2015 Allen Arakaki.  All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ikakara.awsinstance.util

import java.text.ParseException
import java.text.SimpleDateFormat

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * @author Allen
 */
@CompileStatic
@Slf4j("LOG")
class CalendarUtil {
  public static final int MILLIS_PER_SECOND = 1000
  public static final int SECONDS_PER_MINUTE = 60
  public static final int MINUTES_PER_HOUR = 60
  public static final int HOURS_PER_DAY = 24

  public static final String CONCISE_DATETIME_FORMAT = "yyyyMMddHHmmss"
  public static final String CONCISE_DATETIME_FORMAT_MS = "yyyyMMddHHmmssSSS"
  public static final String ISO8601_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
  public static final String DATE_DATETIME_FORMAT = "yyyy-MM-dd"

  static enum ROUND_TYPE {

    SECOND(MILLIS_PER_SECOND), // 1000 ms
    MINUTE(MILLIS_PER_SECOND * SECONDS_PER_MINUTE), // 60 seconds
    HOUR(MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR), // 60 minutes
    DAY(MILLIS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY) // 24 hours

    private final long millis // milliseconds

    ROUND_TYPE(long ms) {
      millis = ms
    }

    long millis() {
      return millis
    }
  }

  static String getStringFromDate_CONCISE(Date date) {
    return getStringFromDate(date, CONCISE_DATETIME_FORMAT)
  }

  static String getStringFromDate_CONCISE_MS(Date date) {
    return getStringFromDate(date, CONCISE_DATETIME_FORMAT_MS)
  }

  static String getStringFromDate_ISO8601(Date date) {
    return getStringFromDate(date, ISO8601_DATETIME_FORMAT)
  }

  static String getStringFromDate(Date date, String format) {
    try {
      return new SimpleDateFormat(format).format(date ?: new Date())
    } catch (e) {
      LOG.error("getStringFromDate:$format", e)
    }
  }

  static Date getDateFromString_CONCISE(String datestr) {
    return getDateFromString(datestr, CONCISE_DATETIME_FORMAT)
  }

  static Date getDateFromString_CONCISE_MS(String datestr) {
    return getDateFromString(datestr, CONCISE_DATETIME_FORMAT_MS)
  }

  static Date getDateFromString_DATE(String datestr) {
    return getDateFromString(datestr, DATE_DATETIME_FORMAT)
  }

  static Date getDateFromString(String datestr, String format) {
    try {
      return new SimpleDateFormat(format).parse(datestr)
    } catch (ParseException ex) {
      LOG.error("getDatefromString:$datestr,$format", ex)
    }
  }

  static Date round(Date inDate, ROUND_TYPE type, int amt) {
    Calendar calendar = Calendar.instance
    if (inDate) {
      calendar.time = inDate
    }

    // determine rounding
    long mod = type.millis() //ms
    mod = mod * amt

    long unroundedMillis = calendar.timeInMillis
    long millisToRound = unroundedMillis % mod
    long roundedMillis = unroundedMillis - millisToRound
    calendar.timeInMillis = roundedMillis

    return calendar.time
  }

  static int date_diff_minutes(Date d1, Date d2) {
    return (int) (getDateDiff(d1, d2, Calendar.MINUTE))
  }

  // Copied from web
  // Use one of the constants from Calendar, e.g. DATE, WEEK_OF_YEAR,
  //  or MONTH, as the calUnit variable.  Supply two Date objects.
  //  This method returns the number of days, weeks, months, etc.
  //  between the two dates.  In other words it returns the result of
  //  subtracting two dates as the number of days, weeks, months, etc.
  static long getDateDiff(Date d1, Date d2, int calUnit) {
    if (d1.after(d2)) {    // make sure d1 < d2, else swap them
      Date temp = d1
      d1 = d2
      d2 = temp
    }
    GregorianCalendar c1 = new GregorianCalendar()
    c1.time = d1
    GregorianCalendar c2 = new GregorianCalendar()
    c2.time = d2
    for (long i = 1;; i++) {
      c1.add(calUnit, 1)   // add one day, week, year, etc.
      if (c1.after(c2)) {
        return i - 1
      }
    }
  }
}
