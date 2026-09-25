package se.alipsa.gi.fx

import groovy.transform.CompileStatic

/**
 * Bounds-safe accessors for the row and column-type lists a table view is built
 * from. Ragged data would otherwise throw IndexOutOfBoundsException from inside a
 * JavaFX cell value factory, where it cannot be handled.
 *
 * This class deliberately imports nothing from JavaFX so it can be unit-tested
 * without a display.
 */
@CompileStatic
class TableData {

  /** Returns the cell text at index, or an empty string when the row is shorter. */
  static String cellAt(List<String> row, int index) {
    return row != null && index >= 0 && index < row.size() ? row.get(index) : ''
  }

  /**
   * Returns the column type name at index, or STRING when the type list is
   * shorter. STRING is the safe default: it is not in the numeric type list, so
   * the column is left-aligned.
   */
  static String typeAt(List<String> types, int index) {
    return types != null && index >= 0 && index < types.size() ? types.get(index) : 'STRING'
  }
}
