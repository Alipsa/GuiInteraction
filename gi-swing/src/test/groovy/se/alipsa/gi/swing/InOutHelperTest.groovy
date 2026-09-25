package se.alipsa.gi.swing

import org.junit.jupiter.api.Test
import se.alipsa.groovy.svg.Svg

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

  @Test
  void noColumnsMeansNoGeneratedNames() {
    assertEquals([], InOut.defaultColumnNames(0))
    assertEquals([], InOut.defaultColumnNames(-1))
  }

  @Test
  void generatedNamesAreOneBasedAndAscending() {
    assertEquals(['c1', 'c2', 'c3'], InOut.defaultColumnNames(3))
  }

  @Test
  void theColumnCountCoversTheWidestRowNotJustTheFirst() {
    assertEquals(0, InOut.widestRow([]))
    assertEquals(0, InOut.widestRow([[]]))
    assertEquals(3, InOut.widestRow([[1], [1, 2, 3], [1, 2]]))
    assertEquals(2, InOut.widestRow([[1, 2], null]))
  }

  @Test
  void alignmentFlagsNeverOutrunTheColumnModel() {
    // view(Matrix) derives rightAlign from the first row but columns from
    // columnNames(); a short first row would otherwise index past the model.
    assertEquals([true, false], InOut.alignmentFlags([true, false, true], 2))
    assertEquals([true], InOut.alignmentFlags([true, false], 1))
    assertEquals([], InOut.alignmentFlags([true], 0))
    assertEquals([true], InOut.alignmentFlags([true], 5))
    assertEquals([], InOut.alignmentFlags(null, 3))
  }

  @Test
  void anExplicitTitleWinsOverTheSvgTitle() {
    Svg svg = new Svg()
    svg.addTitle('from svg')

    assertEquals('chosen', InOut.svgTitle(svg, 'chosen'))
  }

  @Test
  void aTitlelessSvgYieldsNullInsteadOfThrowing() {
    assertNull(InOut.svgTitle(new Svg()))
    assertNull(InOut.svgTitle(null))
  }

  @Test
  void theSvgTitleIsUsedWhenNoTitleIsSupplied() {
    Svg svg = new Svg()
    svg.addTitle('from svg')

    assertEquals('from svg', InOut.svgTitle(svg))
  }
}
