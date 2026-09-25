package se.alipsa.gi.fx

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

/**
 * Unit tests for the pure helpers of {@link InOut}. The InOut constructor throws
 * when headless, and this module runs its tests with java.awt.headless=true, so
 * only static helpers can be exercised here.
 */
class InOutHelperTest {

  @Test
  void aMissingFilterDescriptionFallsBackToANonEmptyLabel() {
    assertFalse(InOut.filterDescription(null).isEmpty())
    assertFalse(InOut.filterDescription('').isEmpty())
    assertFalse(InOut.filterDescription('   ').isEmpty())
  }

  @Test
  void aSuppliedFilterDescriptionIsPassedThrough() {
    assertEquals('Data files', InOut.filterDescription('Data files'))
  }
}
