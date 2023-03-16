import java.util.*

class Cell () {
    private var opened: Boolean = false
    private var mine: Boolean = false
    private var marked: Boolean = false
    private var suspicious: Boolean = false
    private var near: Int = 0
    fun dig() {
        opened = true
        suspicious = false
    }
    fun isDigged():Boolean {
        return opened
    }
    fun setMine() {
        mine = true
    }
    fun getMine(): Boolean {
        return mine
    }
    fun setFlag() {
        marked = !marked
        if (marked) suspicious = false
    }
    fun getFlag():Boolean {
        return marked
    }
    fun setSuspicious() {
        suspicious = !suspicious
    }
    fun setCount(count: Int) {
        near = count
    }
    fun getCount():Int {
        return near
    }
    fun print():String {
        return when {
            marked -> "▲"
            suspicious -> "△"
            !opened -> "░"
            mine -> "●"
            near == 0 -> " "
            else -> near.toString()
        }
    }


}

class Minesweeper(private val rows: Int, private val cols: Int, private val mines: Int) {
    private val board = Array(rows) { Array(cols) {Cell()} }
    private var gameOver = false

    init {
        placeMines()
        placeNumbers()
    }

    fun play() {
        while (!gameOver) {
            displayBoard()
            val (row, col, action) = getUserInput()
            if (action == "F") {
                markCell(row, col)
            } else if (action == "S") {
                markSuspicious(row, col)
            } else if (action == "D") {
                if (!board[row][col].getFlag()) {
                    if (board[row][col].getMine()) {
                        gameOver = true
                        revealAll()
                        removeFlags()
                        displayBoard()
                        println("""
                            ____
                     __,-~~/~    `---.
                   _/_,---(      ,    )
               __ /        <    /   )  \___
- ------===;;;'====------------------===;;;===----- -  -
                  \/  ~"~"~"~"~"~\~"~)~"/
                  (_ (   \  (     >    \)
                   \_( _ <         >_>'
                      ~ `-i' ::>|--"
                          I;|.|.|
                         <|i::|i|`.
                        (` ^'"`-' ")
                 ▄▄▄▄·             • ▌ ▄ ·. 
                 ▐█ ▀█▪▪     ▪     ·██ ▐███▪
                 ▐█▀▀█▄ ▄█▀▄  ▄█▀▄ ▐█ ▌▐▌▐█·
                 ██▄▪▐█▐█▌.▐▌▐█▌.▐▌██ ██▌▐█▌
                 ·▀▀▀▀  ▀█▄▀▪ ▀█▄▀▪▀▀  █▪▀▀▀
                        """)
                        println("RIP")
                    } else {
                        if (board[row][col].isDigged()){
                            digAround(row, col)
                        }
                        revealCell(row, col)
                        if (checkWin()) {
                            gameOver = true
                            revealAll()
                            removeFlags()
                            displayBoard()
                            println("""
       ____                            _       _ 
      / ___|___  _ __   __ _ _ __ __ _| |_ ___| |
     | |   / _ \| '_ \ / _` | '__/ _` | __/ __| |
     | |__| (_) | | | | (_| | | | (_| | |_\__ \_|
      \____\___/|_| |_|\__, |_|  \__,_|\__|___(_)
                       |___/                     
    """)
                            println("Now this field is safe to grow potatoes!")
                        }
                    }
                }
            }
        }
    }

    private fun placeMines() {
        var minesToPlace = mines
        val random = Random()
        while (minesToPlace > 0) {
            val row = random.nextInt(rows)
            val col = random.nextInt(cols)
            if (!board[row][col].getMine()) {
                board[row][col].setMine()
                minesToPlace--
            }
        }
    }

    private fun placeNumbers() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (board[row][col].getMine()) continue
                var count = 0
                for (r in row - 1..row + 1) {
                    for (c in col - 1..col + 1) {
                        if (r in 0 until rows && c in 0 until cols && board[r][c].getMine()) {
                            count++
                        }
                    }
                }
                board[row][col].setCount(count)
            }
        }
    }

    private fun displayBoard() {
        if (cols>10) {
            print("   ┃                     ")
            (11 .. cols).forEach { print("1 ") }
            println("┃")
        }
        print("   ┃ ")
        (0 until cols).forEach { print("${ it % 10 } ") }
        println("┃ ● $mines")
        println("━━━╋${ "━".repeat(cols * 2) }━╋━━━")
        for (row in 0 until rows) {
            if (row<10) print(" ")
            println("$row ┃ ${ printRow(row) }┃ $row")
        }
        println("━━━╋${ "━".repeat(cols * 2) }━╋━━━")
        if (cols>10) {
            print("   ┃ ")
            (0 until 10).forEach { print("$it ") }
            (10 until  cols).forEach { print("1 ") }
            println("┃")
            print("   ┃                     ")
            (10 until  cols).forEach { print("${it % 10 } ") }
        } else {
            print("   ┃ ")
            (0 until cols).forEach { print("$it ") }
        }
        println("┃ ▲ ${countFlags()}")

    }

    private fun printRow(row:Int):String {
        var str: String = ""
        for (col in 0 until board[row].size) {
            str += board[row][col].print() + " "
        }
        return str
    }

    private fun countFlags():Int {
        var count = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (board[row][col].getFlag()) count++
            }
        }
        return count
    }

    private fun getUserInput(): Triple<Int, Int, String> {
        while (true) {
            println("Enter row, column and action")
            print("(F=flag, S=suspicious, D=dig) (e.g. 0 0 F): ")
            val input = readLine()?.split(' ', limit=3)
            val inpRow = input?.getOrNull(0)?.toIntOrNull()
            val inpCol = input?.getOrNull(1)?.toIntOrNull()
            var inpAction = input?.getOrNull(2)
            if (inpRow in 0 until rows && inpRow !=null && inpCol in 0 until cols && inpCol != null && inpAction != null) {
                inpAction = inpAction.uppercase().trim()
                if (inpAction == "F" || inpAction == "S" || inpAction == "D") {
                    return Triple(inpRow, inpCol, inpAction)
                }
            }
            println("Invalid input, try again.")
        }
    }

    private fun revealCell(row: Int, col: Int) {
        if (board[row][col].isDigged()) return
        if (board[row][col].getFlag()) return
        board[row][col].dig()
        if (board[row][col].getCount() == 0) {
            for (r in row - 1..row + 1) {
                for (c in col - 1..col + 1) {
                    if (r in 0 until rows && c in 0 until cols) {
                        revealCell(r, c)
                    }
                }
            }
        }
    }

    private fun digAround(row: Int, col: Int) {
        // count flags
        var count = 0
        for (r in row - 1..row + 1) {
            for (c in col - 1..col + 1) {
                if (r in 0 until rows && c in 0 until cols && board[r][c].getFlag()) {
                    count++
                }
            }
        }
        if (count == board[row][col].getCount()) {
            // dig around
            for (r in row - 1..row + 1) {
                for (c in col - 1..col + 1) {
                    if (r in 0 until rows && c in 0 until cols && !board[r][c].getFlag()) {
                        revealCell(r, c)
                    }
                }
            }
        }
    }

    private fun markCell(row: Int, col: Int) {
        if (!board[row][col].isDigged()) board[row][col].setFlag()
    }

    private fun markSuspicious(row: Int, col: Int) {
        if (!board[row][col].isDigged() && !board[row][col].getFlag()) {
            board[row][col].setSuspicious()
        }
    }

    private fun revealAll() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                board[row][col].dig()
            }
        }
    }

    private fun removeFlags() {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (board[row][col].getFlag()) board[row][col].setFlag()
            }
        }
    }

    private fun checkWin(): Boolean {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (!board[row][col].isDigged() && !board[row][col].getMine()) {
                    return false
                }
            }
        }
        return true
    }

}

fun getGameConfig(): Triple<Int, Int, Int> {
    while (true) {
        println("Enter number of rows, columns and mines (e.g. 5 5 5)")
        print("Size range 2x2 to 20x20, mines 1-399: ")
        val input = readLine()?.split(' ')
        val inpRow = input?.getOrNull(0)?.toIntOrNull()
        val inpCol = input?.getOrNull(1)?.toIntOrNull()
        val inpMines = input?.getOrNull(2)?.toIntOrNull()
        if (inpRow in 2..20 && inpRow !=null && inpCol in 2..20 && inpCol != null ) {
            if (inpMines != null && inpMines in 1 until inpCol*inpCol) return Triple(inpRow, inpCol, inpMines)
        }
        println("Invalid input, try again.")
    }
}

fun askRetry(): Boolean {
    while (true) {
        print("Do you want to play one more time? (y/n): ")
        val input = readLine()?.split(' ', limit=1)
        val inpAnswer = input?.getOrNull(0)?.uppercase()
        if (inpAnswer !=null) {
            if (inpAnswer == "Y" || inpAnswer == "YES") return true
            if (inpAnswer == "N" || inpAnswer == "NO") return false
        }
        println("Invalid input, try again.")
    }
}

fun main() {
    println("""                                               
       _                                       
 _____|_|___ ___ ___ _ _ _ ___ ___ ___ ___ ___ 
|     | |   | -_|_ -| | | | -_| -_| . | -_|  _|
|_|_|_|_|_|_|___|___|_____|___|___|  _|___|_|  
                                  |_|          
""")
    do {
        val (rows, cols, mines) = getGameConfig()
        val game = Minesweeper(rows, cols, mines)
        game.play()
    } while (askRetry())
}