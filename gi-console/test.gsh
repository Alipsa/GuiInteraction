@Grab("se.alipsa.gi:gi-console:1.0.0-SNAPSHOT")
def io = new se.alipsa.gi.txt.InOut()

def file = io.chooseFile("Choose a file", ".", "" )
println("File chosen was $file")