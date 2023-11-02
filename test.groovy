import java.time.LocalDate
import java.time.YearMonth

//def io = new se.alipsa.gi.swing.InOut()
def io = new se.alipsa.gi.fx.InOut()

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

/*
def html = """
<html>
    <body>
        <h1>Hello world</h1>
        <div>A simple text</div>
        <table>
            <tr><td>1</td><td>Per</td></tr>
            <tr><td>2</td><td>Louise</td></tr>
        </table>
    </body>
</html>
"""
File file = new File("/tmp/t.html")
file.write(html)
io.view(file, "Example html page")

io.view(html, "View of html content")
 */

/*
io.viewMarkdown("""# example title
- one thing
- another thing

## Ordered list
1. *this*
2. _that_
""", 'Markdown example')
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
    "Matrix example",
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
io.view(rowList, 'List of lists example')
*/

//io.view(123, "apples")

//io.display(io.projectFile("src/test/resources/svgplot.svg"))
//io.display(io.projectFile("src/test/resources/areachart2.png"))

import se.alipsa.groovy.matrix.*
import se.alipsa.groovy.charts.*
import static se.alipsa.groovy.matrix.ListConverter.*
empData = Matrix.create(
        emp_id: 1..5,
        emp_name: ["Rick","Dan","Michelle","Ryan","Gary"],
        salary: [623.3,515.2,611.0,729.0,843.25],
        start_date: toLocalDates("2012-01-01", "2013-09-23", "2014-11-15", "2014-05-11", "2015-03-27"),
        [int, String, Number, LocalDate]
)

chart = AreaChart.create("Salaries", empData, "emp_name", "salary")
io.display(chart, "Matrix charts areachart")

import tech.tablesaw.api.*
empData = Table.create(
        IntColumn.create('emp_id', 1..5 as int[]),
        StringColumn.create('emp_name', ["Rick","Dan","Michelle","Ryan","Gary"]),
        DoubleColumn.create('salary', [623.3,515.2,611.0,729.0,843.25]),
        DateColumn.create('start_date', toLocalDates("2012-01-01", "2013-09-23", "2014-11-15", "2014-05-11", "2015-03-27"))
)
chart = tech.tablesaw.chart.AreaChart.create("Salaries", empData, "emp_name", "salary")
io.display(chart, "tablesaw areachart")