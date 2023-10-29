import java.time.LocalDate
import java.time.YearMonth

def io = new se.alipsa.gi.InOut()

//def file = io.chooseFile("Choose a file", ".", "Pick a file please!" )
//println("File chosen was $file")

//def ym = io.promptYearMonth("Pick a yearmonth")
//println("You picked $ym")

/*
ym = io.promptYearMonth("Yearmonth picker example", "Pick a yearmonth please",
    YearMonth.now().minusYears(1),
    YearMonth.now().plusMonths(2),
    YearMonth.now().minusMonths(1)
)
println("This time, you picked $ym")
 */
/*
def date = io.promptDate("Datepicker example", "Pick a date please", LocalDate.now().minusDays(1))
println("You picked $date")

 */

/*
def option = io.promptSelect("Select example",
    "Choose your afternoon snack",
    "Pick a fruit please",
    ["Apple", "Apricot", "Orange"],
    "Apricot"
)
println("You wanted an $option this afternoon")
 */

/*
def pwd = io.promptPassword("Password demo", "Enter password:")
println("The password is $pwd")
 */

/*
def name = io.prompt("What is your first name?")
println ("Hi $name")

def input = io.prompt("Prompt example", "What is your favorite name?", "Name:")
println("Your favorite name is $input")

def day = io.prompt("Prompt example","What is your favorite day?", "Day", "Monday")
println("Your favorite day is $day")

 */

//io.view(new File("/tmp/t.html"), "Example html page")

/*
io.viewMarkdown("""# example title
- one thing
- another thing

## Ordered list
1. *this*
2. _that_
""")
 */

/*
import tech.tablesaw.api.*
Table table = Table.create("Tablesaw Example",
    IntColumn.create("id", [1,2,3] as int[]),
    StringColumn.create("name", ["Foo", "Bar", "Baz"] as String[]),
    DoubleColumn.create("expense", [123.23, 212.31, 12.1] as double[])
)
io.view(table)
*/
/*
import se.alipsa.groovy.matrix.Matrix
Matrix matrix = Matrix.create(
    "Matrix exmple",
    [
        id: [1,2,3] as int[],
        name: ["Foo", "Bar", "Baz"],
        expense: [123.23, 212.31, 12.1] as double[]
    ], [int, String, double]
)
io.view(matrix)
*/
/*
def rowList = [
    [1, "Foo", 123.23],
    [2, "Bar", 212.31],
    [3, "Baz", 12.1]
]
io.view(rowList)
*/

//io.view(123, "apples")

//io.display(io.projectFile("src/test/resources/svgplot.svg"))
//io.display(io.projectFile("src/test/resources/areachart2.png"))