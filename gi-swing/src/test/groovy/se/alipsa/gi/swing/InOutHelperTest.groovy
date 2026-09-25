package se.alipsa.gi.swing

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

/**
 * Unit tests for the pure helpers of {@link InOut}. The InOut constructor throws
 * when headless, and this module runs its tests with java.awt.headless=true, so
 * only static helpers can be exercised here.
 */
class InOutHelperTest {

  @Test
  void aMissingInitialDirectoryBecomesNullRatherThanThrowing() {
    assertNull(InOut.toInitialDirectory(null))
    assertNull(InOut.toInitialDirectory(''))
    assertNull(InOut.toInitialDirectory('   '))
  }

  @Test
  void aSuppliedInitialDirectoryIsPassedThrough() {
    assertEquals(new File('/tmp'), InOut.toInitialDirectory('/tmp'))
  }
}
