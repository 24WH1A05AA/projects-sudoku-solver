package com.example.sudokusolver

// ---------- Core Sudoku Logic ----------
fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
    if (num == 0) return true
    for (x in 0 until 9) {
        if (board[row][x] == num || board[x][col] == num) return false
    }
    val sr = row - row % 3
    val sc = col - col % 3
    for (i in 0 until 3) for (j in 0 until 3) {
        if (board[sr + i][sc + j] == num) return false
    }
    return true
}

fun solveSudoku(board: Array<IntArray>): Boolean {
    for (r in 0 until 9) for (c in 0 until 9) {
        if (board[r][c] == 0) {
            for (n in 1..9) {
                if (isSafe(board, r, c, n)) {
                    board[r][c] = n
                    if (solveSudoku(board)) return true
                    board[r][c] = 0
                }
            }
            return false
        }
    }
    return true
}

fun isValidBoard(board: Array<IntArray>): Boolean {
    // rows
    for (r in 0 until 9) {
        val seen = BooleanArray(10)
        for (c in 0 until 9) {
            val v = board[r][c]
            if (v != 0) {
                if (seen[v]) return false
                seen[v] = true
            }
        }
    }
    // cols
    for (c in 0 until 9) {
        val seen = BooleanArray(10)
        for (r in 0 until 9) {
            val v = board[r][c]
            if (v != 0) {
                if (seen[v]) return false
                seen[v] = true
            }
        }
    }
    // boxes
    for (sr in 0 until 9 step 3) for (sc in 0 until 9 step 3) {
        val seen = BooleanArray(10)
        for (i in 0 until 3) for (j in 0 until 3) {
            val v = board[sr + i][sc + j]
            if (v != 0) {
                if (seen[v]) return false
                seen[v] = true
            }
        }
    }
    return true
}

// Returns the first empty cell's coordinates as a hint (row, col) using solved board
fun getHint(board: Array<IntArray>): Pair<Int, Int>? {
    val solved = Array(9) { i -> board[i].clone() }
    if (!solveSudoku(solved)) return null
    for (r in 0 until 9) for (c in 0 until 9) {
        if (board[r][c] == 0) return r to c
    }
    return null
}

fun calculateScore(mistakes: Int, hintsUsed: Int, elapsedSeconds: Int): Int {
    val base = 1000
    val mistakePenalty = mistakes * 50
    val hintPenalty = hintsUsed * 100
    val timeBonus = maxOf(0, 600 - elapsedSeconds) // up to +600 for finishing under 10 mins
    return (base - mistakePenalty - hintPenalty + timeBonus).coerceAtLeast(0)
}

// ---------- Sample Puzzles ----------
val easyPuzzle = arrayOf(
    intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
    intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
    intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
    intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
    intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
    intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
    intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
    intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
    intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
)

val mediumPuzzle = arrayOf(
    intArrayOf(0, 0, 0, 2, 6, 0, 7, 0, 1),
    intArrayOf(6, 8, 0, 0, 7, 0, 0, 9, 0),
    intArrayOf(1, 9, 0, 0, 0, 4, 5, 0, 0),
    intArrayOf(8, 2, 0, 1, 0, 0, 0, 4, 0),
    intArrayOf(0, 0, 4, 6, 0, 2, 9, 0, 0),
    intArrayOf(0, 5, 0, 0, 0, 3, 0, 2, 8),
    intArrayOf(0, 0, 9, 3, 0, 0, 0, 7, 4),
    intArrayOf(0, 4, 0, 0, 5, 0, 0, 3, 6),
    intArrayOf(7, 0, 3, 0, 1, 8, 0, 0, 0)
)

val hardPuzzle = arrayOf(
    intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 0, 3, 0, 8, 5),
    intArrayOf(0, 0, 1, 0, 2, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 5, 0, 7, 0, 0, 0),
    intArrayOf(0, 0, 4, 0, 0, 0, 1, 0, 0),
    intArrayOf(0, 9, 0, 0, 0, 0, 0, 0, 0),
    intArrayOf(5, 0, 0, 0, 0, 0, 0, 7, 3),
    intArrayOf(0, 0, 2, 0, 1, 0, 0, 0, 0),
    intArrayOf(0, 0, 0, 0, 4, 0, 0, 0, 9)
)
