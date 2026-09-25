package se.alipsa.gi.fx

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class InOutHeadlessTest {

  @Test
  void headlessConstructionReportsTheExpectedException() {
    assertThrows(UnsupportedOperationException) {
      new InOut()
    }
  }

  @Test
  void onlyExistingReadableRegularFilesAreViewable(@TempDir File tempDir) {
    File readable = new File(tempDir, 'readable.html')
    readable.text = '<p>ok</p>'

    assertFalse(InOut.isViewableFile(null))
    assertFalse(InOut.isViewableFile(new File(tempDir, 'missing.html')))
    assertFalse(InOut.isViewableFile(tempDir))
    assertTrue(InOut.isViewableFile(readable))
  }
}
