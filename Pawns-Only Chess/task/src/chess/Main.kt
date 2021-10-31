package chess

fun main() {
    println( " Pawns-Only Chess")

    println("First Player's name:")
    val firstPlayer = readLine()!!
    println("Second Player's name:")
    val secondPlayer = readLine()!!

    val game = Game(Board(), firstPlayer, secondPlayer)

    game.board.print()

    do {
        println("${game.currentPlayer}'s turn:")
        val input = readLine()!!
        if (input == "exit") println("Bye!")
        else {
            val move = game.parseInputOrNull(input) // nullable
            if (move == null || !game.validMove(move)) println("Invalid Input")
            else if (!game.isPawnAt(move.from)) println("No ${game.side} pawn at ${move.from}")
            else if (move.from == move.to) println("Invalid Input") // тот самый костыль про b3b3
            else if (!game.freeAt(move.to)) println("Invalid Input")
            else {
                game.makeTurn(move)
                game.board.print()
            }
        }
    } while (input != "exit")
}

class Game(val board: Board, val whitePlayer: String, val blackPlayer: String, ) {
    var side: String = "white"  // or black
    val currentPlayer: String
        get() = if (side == "white") whitePlayer else blackPlayer

    fun parseInputOrNull(input: String): Move? {
        return if (input.matches("[a-h][1-8][a-h][1-8]".toRegex())) {
            Move(input)
        } else null
    }

    fun validMove(move: Move): Boolean {
        if (move.from.fileIndex != move.to.fileIndex) return false

        val dir = if (side == "white") 1 else -1
        val steps = (move.to.rankIndex - move.from.rankIndex) * dir

        if (steps == 0) return true // лютый костыль, потому что в тестах b3b3 -> no white pawn at b3

        if (steps == 1) return true

        if (steps == 2)
            if (side == "white") return move.from.rankIndex == 1
            else return move.from.rankIndex == 6
        else return false
    }

    fun isPawnAt(from: Position): Boolean {
        val square = board.getSquare(from)

        return square == 'W' && side == "white" || square == 'B' && side == "black"
    }

    fun freeAt(to: Position): Boolean {
        val square = board.getSquare(to)

        return square == ' '
    }

    fun makeTurn(move: Move) {
        board.clearSquare(move.from)

        board.setSquare(move.to, if (side == "white") 'W' else 'B')

        side = if (side == "white") "black" else "white"    // switch sides
    }
}

class Board {
    val ranks = List(8) { rankIndex->
        when(rankIndex) {
            6 -> Rank(7, 'B')
            1 -> Rank(2, 'W')
            else -> Rank(rankIndex + 1, ' ')
        }
    }

    fun getSquare(pos: Position) = ranks[pos.rankIndex].files[pos.fileIndex]
    fun clearSquare(pos: Position) {
        ranks[pos.rankIndex].files[pos.fileIndex] = ' '
    }

    fun setSquare(pos: Position, c: Char) {
        ranks[pos.rankIndex].files[pos.fileIndex] = c
    }

    fun print() {
        Rank.printSeparator()
        for (rankIndex in 7 downTo 0) {
            ranks[rankIndex].printFiles()
            Rank.printSeparator()
        }

        Rank.printFooter()
    }
}

class Rank(private val rank: Int, fill: Char) {
    val files = MutableList(8) { fill }

    fun printFiles() {
        print("$rank ")
        for (fileIndex in 0..7)
            print("| ${files[fileIndex]} ")
            println("|")
    }

    companion object {
        fun printSeparator() = println("  +---+---+---+---+---+---+---+---+")
        fun printFooter() = println( "    a   b   c   d   e   f   g   h")
    }
}

class Move(s4: String) {
    val from = Position(s4.take(2))
    val to = Position(s4.takeLast(2))
}

class Position(s2: String) {
    val fileIndex = s2.first() - 'a'
    val rankIndex = s2.last().digitToInt() - 1

    override fun toString() = "${'a' + fileIndex}${rankIndex + 1}"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Position

        if (fileIndex != other.fileIndex) return false
        if (rankIndex != other.rankIndex) return false

        return true
    }
}
