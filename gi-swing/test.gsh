@Grab("se.alipsa.gi:gi-swing:1.0.0-SNAPSHOT")
def io = new se.alipsa.gi.swing.InOut()

def file = io.chooseFile("Choose a file", ".", "Pick a file please!" )
println("File chosen was $file")