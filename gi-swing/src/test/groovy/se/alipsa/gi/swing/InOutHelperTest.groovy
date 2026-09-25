package se.alipsa.gi.swing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import se.alipsa.groovy.svg.Svg

import javax.swing.JEditorPane

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

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

  @Test
  void loadingAMissingPageReportsFailureInsteadOfThrowing() {
    assertFalse(InOut.loadPage(new JEditorPane(), new File('/nonexistent/missing.html')))
  }

  @Test
  void loadingARealPageReportsSuccess(@TempDir File tempDir) {
    File page = new File(tempDir, 'page.html')
    page.text = '<html><body><h1>ok</h1></body></html>'

    assertTrue(InOut.loadPage(new JEditorPane(), page))
  }

  @Test
  void svgIsDetectedByContentTypeOrByName() {
    URL svgByName = URI.create('file:/tmp/plot.svg').toURL()
    URL pngByName = URI.create('file:/tmp/plot.png').toURL()

    assertTrue(InOut.isSvg('image/svg+xml', pngByName), 'content type wins')
    assertTrue(InOut.isSvg('application/xml', svgByName), 'name is the fallback')
    assertTrue(InOut.isSvg(null, svgByName), 'name is used when detection failed')
    assertFalse(InOut.isSvg('image/png', pngByName))
    assertFalse(InOut.isSvg(null, pngByName))
  }
}
