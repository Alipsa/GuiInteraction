import javafx.application.Platform

@Grab("se.alipsa.gi:gi-fx:1.0.0-SNAPSHOT")
def io = new se.alipsa.gi.fx.InOut()

def file = io.chooseFile("Choose a file", ".", "Pick a file please!" )
println("File chosen was $file")

// gi-fx is meant to be used from a javafx application
// since this is not we must explicitly shut down
Platform.exit()