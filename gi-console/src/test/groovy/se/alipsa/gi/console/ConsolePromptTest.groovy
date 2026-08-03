package se.alipsa.gi.console

import org.junit.jupiter.api.Test

import java.time.LocalDate
import java.time.YearMonth

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows

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
  void rangedYearMonthRejectsValuesOutsideRange() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader('2027-01\n'))

    assertThrows(IllegalArgumentException) {
      inOut.promptYearMonth('title', 'month', YearMonth.of(2025, 1), YearMonth.of(2026, 12), null)
    }
  }

  @Test
  void fullSelectionValidatesOptionsAndHandlesEof() {
    InOut inOut = new InOut()
    inOut.sysin = new BufferedReader(new StringReader(''))

    assertEquals('first', inOut.promptSelect('title', '', 'choice', ['first', 'second'], 'first'))
    assertThrows(IllegalArgumentException) {
      inOut.promptSelect('title', '', 'choice', [], null)
    }
  }
}
