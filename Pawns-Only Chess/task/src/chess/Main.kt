package chess

import kotlin.math.abs

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
            if (move == null) println("Invalid Input")
            else if (!game.isPawnAt(move.from)) println("No ${game.side} pawn at ${move.from}")
            else if (!game.validMove(move)) println("Invalid Input")
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
    var canEnPassant = false
    var enPassantFileIndex = 0

    fun parseInputOrNull(input: String): Move? {
        return if (input.matches("[a-h][1-8][a-h][1-8]".toRegex())) {
            Move(input)
        } else null
    }

    fun validMove(move: Move): Boolean {
        with(move) {
            val dir = if (side == "white") 1 else -1
            val steps = (to.rankIndex - from.rankIndex) * dir

            if (from.fileIndex == to.fileIndex) {
                // Move forward
                if (steps == 1) return isFreeAt(to)

                else if (steps == 2
                    && from.rankIndex == if (side == "white")  1 else 6) return isFreeAt(to)

                else return false
            } else if (abs(from.fileIndex - to.fileIndex) == 1 && to.fileIndex in 0..7) {
                // Capture
                if (steps == 1) return isEnPassant(to) || isOpponentPawnAt(to)

                else return false
            } else return false
        }
    }

    fun isPawnAt(from: Position): Boolean {
        val square = board.getSquare(from)

        return square == 'W' && side == "white" || square == 'B' && side == "black"
    }

    fun isOpponentPawnAt(to: Position): Boolean {
        val square = board.getSquare(to)

        return square == 'B' && side == "white" || square == 'W' && side == "black"
    }

    fun isFreeAt(to: Position): Boolean {
        val square = board.getSquare(to)

        return square == ' '
    }

    fun isEnPassant(to: Position): Boolean {
        return canEnPassant && to.fileIndex == enPassantFileIndex &&
                to.rankIndex == if (side == "white") 5 else 2
    }

    fun makeTurn(move: Move) {
        with(move) {
            board.clearSquare(from)

            board.setSquare(to, if (side == "white") 'W' else 'B')

            if (isEnPassant(to)) {
                val opponentPawnPosition = Position(fileIndex = to.fileIndex, rankIndex = from.rankIndex)
                board.clearSquare(opponentPawnPosition)
            }

            if (from.fileIndex == to.fileIndex && abs(to.rankIndex - from.rankIndex) == 2) {
                canEnPassant = true
                enPassantFileIndex = from.fileIndex
            } else canEnPassant = false
        }

        side = if (side == "white") "black" else "white"    // switch sides
    }
}

class Board {
    private val ranks = List(8) { rankIndex->
        when(rankIndex) {
            6 -> Rank(7, 'B')
            1 -> Rank(2, 'W')
            else -> Rank(rankIndex + 1, ' ')
        }
    }

    fun getSquare(pos: Position) = ranks[pos.rankIndex].files[pos.fileIndex]

    fun setSquare(pos: Position, c: Char) {
        ranks[pos.rankIndex].files[pos.fileIndex] = c
    }

    fun clearSquare(pos: Position) {
        setSquare(pos, ' ')
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

class Position(val fileIndex: Int, val rankIndex: Int) {

    constructor(s2: String) : this(
        s2.first() - 'a',
        s2.last().digitToInt() - 1
    )

    override fun toString() = "${'a' + fileIndex}${rankIndex + 1}"
}
