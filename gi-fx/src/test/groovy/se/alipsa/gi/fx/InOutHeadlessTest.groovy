package se.alipsa.gi.fx

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class InOutHeadlessTest {

  @Test
  void headlessConstructionReportsTheExpectedException() {
    assertThrows(UnsupportedOperationException) {
      new InOut()
    }
  }
}
