package se.alipsa.gi.fx

import org.junit.jupiter.api.Test
import se.alipsa.matrix.core.Matrix

import java.text.NumberFormat

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow

class TableDataTest {

  @Test
  void unnamedAndExplicitMatrixTitles() {
    Matrix unnamed = Matrix.builder().rows([[1, 2]]).build()
    Matrix named = Matrix.builder().matrixName('from matrix').rows([[1, 2]]).build()
    assertEquals('Table', Viewer.matrixTitle(unnamed))
    assertEquals('Table', Viewer.matrixTitle(null))
    assertEquals('chosen', Viewer.matrixTitle(named, 'chosen'))
    assertEquals('from matrix', Viewer.matrixTitle(named, '   '))
  }

  @Test
  void viewingANullMatrixIsIgnoredRatherThanThrowing() {
    assertDoesNotThrow({ Viewer.viewTable((Matrix) null) } as org.junit.jupiter.api.function.Executable)
  }

  @Test
  void aRaggedGridKeepsEveryColumnAndRendersNullRowsAsEmpty() {
    List<List<?>> rows = [['first'], null, ['second', 'third']]
    assertEquals(2, Viewer.widestRow(rows))
    assertEquals([], Viewer.formatRow(rows[1], NumberFormat.getInstance()))
    assertEquals('', TableData.cellAt(Viewer.formatRow(rows[0], NumberFormat.getInstance()), 1))
  }

  @Test
  void theGridColumnCountCoversTheWidestNonEmptyRow() {
    assertEquals(0, Viewer.widestRow([[]]))
    assertEquals(3, Viewer.widestRow([[1], [2, 3, 4], []]))
    assertEquals(2, Viewer.widestRow([[1, 2], null]))
  }

  @Test
  void aNullRowFormatsAsAnEmptyRow() {
    NumberFormat numberFormatter = NumberFormat.getInstance()
    numberFormatter.setGroupingUsed(false)

    assertEquals([], Viewer.formatRow(null, numberFormatter))
    assertEquals(['1', 'text'], Viewer.formatRow([1, 'text'], numberFormatter))
  }

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
