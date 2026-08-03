package se.alipsa.gi.swing

import org.junit.jupiter.api.Test

import java.awt.GraphicsEnvironment

import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assumptions.assumeTrue

class InOutHeadlessTest {

  @Test
  void headlessConstructionReportsTheExpectedException() {
    assumeTrue(GraphicsEnvironment.isHeadless())

    assertThrows(UnsupportedOperationException) {
      new InOut()
    }
  }
}
