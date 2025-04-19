import static groovy.grape.Grape.grab

if (! binding.hasVariable('io')) {
  grab(group:"se.alipsa.gi", module: "gi-swing", version:"0.1.0")
  binding.setVariable('io', this.class.classLoader.loadClass("se.alipsa.gi.swing.InOut").getDeclaredConstructor().newInstance())
}

def file = io.chooseFile("Choose a file", ".", "Pick a file please!" )
println("File chosen was $file")