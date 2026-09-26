package se.alipsa.gi.swing

import org.junit.jupiter.api.Test
import se.alipsa.matrix.core.Matrix

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class InOutHeadlessTest {

  @Test
  void aNullMatrixIsRejectedBeforeSwingIsTouched() {
    assertFalse(InOut.canViewMatrix(null))
    assertTrue(InOut.canViewMatrix(Matrix.builder().rows([[1]]).build()))
  }

  @Test
  void headlessConstructionReportsTheExpectedException() {
    assertThrows(UnsupportedOperationException) {
      new InOut()
    }
  }
}
