package se.alipsa.gi.swing

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class InOutSvgTest {

  @Test
  void detectsOnlySvgResourceNames() {
    assertTrue(InOut.isSvgResource(new URL('file:/tmp/report.svg')))
    assertFalse(InOut.isSvgResource(new URL('file:/tmp/report.svg.png')))
    assertFalse(InOut.isSvgResource(new URL('file:/tmp/svgs.svg/logo.png')))
  }
}
