package se.alipsa.gi.fx

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class TableDataTest {

  @Test
  void aCellBeyondTheEndOfARowRendersEmpty() {
    assertEquals('', TableData.cellAt(['a'], 1))
    assertEquals('', TableData.cellAt([], 0))
    assertEquals('', TableData.cellAt(null, 0))
    assertEquals('', TableData.cellAt(['a'], -1))
  }

  @Test
  void aCellWithinARowIsReturnedUnchanged() {
    assertEquals('b', TableData.cellAt(['a', 'b'], 1))
  }

  @Test
  void aMissingColumnTypeDefaultsToStringSoItIsLeftAligned() {
    assertEquals('STRING', TableData.typeAt(['INTEGER'], 1))
    assertEquals('STRING', TableData.typeAt([], 0))
    assertEquals('STRING', TableData.typeAt(null, 0))
  }

  @Test
  void aKnownColumnTypeIsReturnedUnchanged() {
    assertEquals('INTEGER', TableData.typeAt(['INTEGER', 'STRING'], 0))
  }
}
