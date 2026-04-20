package se.alipsa.gi.console;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class InOutTest {

  @Test
  void clipboardOperationsReturnNullWhenHeadless() throws ExecutionException, InterruptedException {
    Assumptions.assumeTrue(GraphicsEnvironment.isHeadless());
    InOut inOut = new InOut();

    assertDoesNotThrow(() -> inOut.saveToClipboard("value"));
    assertDoesNotThrow(() -> inOut.saveToClipboard(new File("dummy.txt")));

    assertNull(inOut.getFromClipboard());
    assertNull(inOut.getFileFromClipboard());
    assertNull(inOut.getImageFromClipboard());
  }
}
