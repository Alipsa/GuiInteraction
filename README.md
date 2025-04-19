# se.alipsa.gi.GuiInteraction
Allows [Gade](https://github.com/Alipsa/gade) Gui Interactive capabilities from a standalone app

There are three versions of it
1. _gi-fx_ This is the most similar module to Gade but requires a JVM with Javafx included (e.g. Bellsoft Full)
2. _gi-swing_ This gives the same functionality as the io in gade but uses swing so works with any JDK but looks slightly different
3. _gi-console_ Best effort in reproducing gui interaction on the console

Using one of these, you can package a standalone application offering the same interaction capabilities as when you are running the code in Gade.

Here is an example:
```groovy
import static groovy.grape.Grape.grab

// This makes the code run equally in Gade and in a standalone Groovy script
if (! binding.hasVariable('io')) {
  grab(group:"se.alipsa.gi", module: "gi-swing", version:"0.1.0")
  binding.setVariable('io', this.class.classLoader.loadClass("se.alipsa.gi.swing.InOut").getDeclaredConstructor().newInstance())
}

def file = io.chooseFile("Choose a file", ".", "Pick a file please!" )
println("File chosen was $file")
```
