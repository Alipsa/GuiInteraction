package se.alipsa.gi.console

import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.YearMonth

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

class ConsolePromptTest {

  @Test
  void eofIsCancellationForPrompts() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader(''))

    assertNull(inOut.prompt('message'))
    assertNull(inOut.prompt('title', 'header', 'message', 'default'))
    assertNull(inOut.promptYearMonth('month'))
    assertNull(inOut.promptDate('date', 'message', LocalDate.of(2026, 1, 2)))
  }

  @Test
  void blankDateAndMonthUseDefaults() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader('\n\n'))

    assertEquals(YearMonth.of(2026, 1),
        inOut.promptYearMonth('title', 'month', YearMonth.of(2025, 1), YearMonth.of(2026, 12), YearMonth.of(2026, 1)))
    assertEquals(LocalDate.of(2026, 1, 2),
        inOut.promptDate('date', 'message', LocalDate.of(2026, 1, 2)))
  }

  @Test
  void rangedYearMonthFallsBackForOutOfRangeValues() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader('2027-01\n'))

    assertEquals(YearMonth.of(2026, 12),
        inOut.promptYearMonth('title', 'month', YearMonth.of(2025, 1), YearMonth.of(2026, 12), YearMonth.of(2026, 12)))
  }

  @Test
  void rangedYearMonthFallsBackForUnparseableValues() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader('not-a-month\n'))

    assertEquals(YearMonth.of(2026, 12),
        inOut.promptYearMonth('title', 'month', YearMonth.of(2025, 1), YearMonth.of(2026, 12), YearMonth.of(2026, 12)))
  }

  @Test
  void fullSelectionValidatesOptionsAndHandlesEof() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader(''))

    assertNull(inOut.promptSelect('title', '', 'choice', ['first', 'second'], 'first'))
    assertThrows(IllegalArgumentException) {
      inOut.promptSelect('title', '', 'choice', [], null)
    }
  }

  @Test
  void displayingAMissingFileReportsItWithoutThrowing() {
    InOut inOut = new InOut()

    assertTrue(captureStdout { inOut.display(new File('/nonexistent/missing.txt')) }
        .contains('does not exist'))
  }

  @Test
  void displayingANullFileReportsItWithoutThrowing() {
    InOut inOut = new InOut()

    assertTrue(captureStdout { inOut.display((File) null) }.contains('does not exist'))
  }

  @Test
  void theDesktopCheckNeverThrows() {
    // Desktop.getDesktop() throws HeadlessException, so isDesktopSupported must gate it.
    assertDoesNotThrow({ InOut.canOpenWithDesktop() } as org.junit.jupiter.api.function.Executable)
  }

  private static String captureStdout(Closure<?> body) {
    PrintStream original = System.out
    ByteArrayOutputStream captured = new ByteArrayOutputStream()
    System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8))
    try {
      body.call()
    } finally {
      System.setOut(original)
    }
    return captured.toString(StandardCharsets.UTF_8)
  }
}
