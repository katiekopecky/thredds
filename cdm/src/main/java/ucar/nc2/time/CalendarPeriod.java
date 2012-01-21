/*
 * Copyright (c) 1998 - 2011. University Corporation for Atmospheric Research/Unidata
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package ucar.nc2.time;

import net.jcip.annotations.Immutable;

/**
 * A CalendarPeriod is a logical duration of time, it requires a Calendar to convert to an actual duration of time.
 * A CalendarField is expressed as {integer x Field}.
 *
 * Design follows joda Period class.
 * @author caron
 * @since 3/30/11
 */
@Immutable
public class CalendarPeriod {
  static private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalendarPeriod.class);
  static public final CalendarPeriod Hour = CalendarPeriod.of(1, Field.Hour);

  public enum Field {
    Millisec, Second, Minute, Hour, Day, Month, Year
  }

  /**
   * Convert a period string into a CalendarPeriod.Field.
   * @param udunit period string
   * @return CalendarPeriod enum
   */
  public static CalendarPeriod.Field fromUnitString(String udunit) {
    udunit = udunit.trim();
    udunit = udunit.toLowerCase();

    if (udunit.equals("s")) return Field.Second;
    if (udunit.equals("ms")) return Field.Millisec;

      // eliminate plurals
    if (udunit.endsWith("s")) udunit = udunit.substring(0, udunit.length()-1);

    if (udunit.equals("second") || udunit.equals("sec")) {
      return Field.Second;
    } else if (udunit.equals("millisec") || udunit.equals("msec")) {
      return Field.Millisec;
    } else if (udunit.equals("minute") || udunit.equals("min")) {
      return Field.Minute;
    } else if (udunit.equals("hour") || udunit.equals("hr") || udunit.equals("h")) {
      return Field.Hour;
    } else if (udunit.equals("day") || udunit.equals("d")) {
      return Field.Day;
    } else if (udunit.equals("month") || udunit.equals("mon")) {
      return Field.Month;
    } else if (udunit.equals("year") || udunit.equals("yr")) {
      return Field.Year;
    } else {
      throw new IllegalArgumentException("cant convert "+ udunit +" to CalendarPeriod");
    }
  }

  public static CalendarPeriod of(int value, Field field) {
    return new CalendarPeriod(value, field);
  }

  ////////////////////////
  // the common case is a single field
  private final int value;
  private final Field field;

  private CalendarPeriod (int value, Field field) {
    this.value = value;
    this.field = field;
  }

  public CalendarPeriod multiply(int value) {
    return new CalendarPeriod(this.value * value, this.field);
  }

  public int getValue() {
    return value;
  }

  public Field getField() {
    return field;
  }

  public int subtract(CalendarDate start, CalendarDate end) {
    if (field == CalendarPeriod.Field.Month || field == CalendarPeriod.Field.Year) {
      log.warn(" CalendarDate.subtracct on Month or Year");
      long diff = end.getDifferenceInMsecs(start);
      return (int) (diff / getValueInMillisecs());
    } else {
      long diff = end.getDifferenceInMsecs(start);
      return (int) (diff / millisecs());

    }
  }

  /**
   * Get the conversion factor of the other CalendarPeriod to this one
   * @param from convert from this
   * @return conversion factor, so that getConvertFactor(from) * from = this
   */
  public double getConvertFactor(CalendarPeriod from) {
    if (field == CalendarPeriod.Field.Month || field == CalendarPeriod.Field.Year) {
      log.warn(" CalendarDate.convert on Month or Year");
    }

    return (double)from.millisecs() / millisecs();
  }

  /**
   * Get the duration in seconds                                               -+
   *
   * @return the duration in seconds
   * @deprecated dont use because these are fixed length and thus approximate.
   */
  public double getValueInMillisecs() {
     if (field == CalendarPeriod.Field.Month)
       return 30.0 * 24.0 * 60.0 * 60.0 * 1000.0 * value;
     else if (field == CalendarPeriod.Field.Year)
       return 365.0 * 24.0 * 60.0 * 60.0 * 1000.0 * value;
     else return millisecs();
   }

  private int millisecs() {
     if (field == CalendarPeriod.Field.Millisec)
       return value;
     else if (field == CalendarPeriod.Field.Second)
       return 1000 * value;
     else if (field == CalendarPeriod.Field.Minute)
       return 60 * 1000 * value;
     else if (field == CalendarPeriod.Field.Hour)
       return 60 * 60 * 1000 * value;
     else if (field == CalendarPeriod.Field.Day)
       return 24 * 60 * 60 * 1000 * value;

     else throw new IllegalStateException("Illegal Field = "+field);
   }

  @Override
  public String toString() {
    return value + " " + field;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CalendarPeriod that = (CalendarPeriod) o;

    if (value != that.value) return false;
    if (field != that.field) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = value;
    result = 31 * result + (field != null ? field.hashCode() : 0);
    return result;
  }
}