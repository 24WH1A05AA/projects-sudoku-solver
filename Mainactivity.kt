package com.example.sudokusolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val highScoreManager = HighScoreManager(this)

        setContent {
            var showLeaderboard by remember { mutableStateOf(false) }
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                if (showLeaderboard) {
                    LeaderboardScreen(highScoreManager) { showLeaderboard = false }
                } else {
                    SudokuApp(highScoreManager) { showLeaderboard = true }
                }
            }
        }
    }
}

@Composable
fun SudokuApp(
    highScoreManager: HighScoreManager,
    onShowLeaderboard: () -> Unit
) {
    var board by remember { mutableStateOf(Array(9) { IntArray(9) { 0 } }) }
    var fixed by remember { mutableStateOf(Array(9) { BooleanArray(9) { false } }) }
    var highlightedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val coroutineScope = rememberCoroutineScope()


    var elapsed by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var mistakes by remember { mutableIntStateOf(0) }
    var hintsUsed by remember { mutableIntStateOf(0) }
    val maxHints = 3
    var score by remember { mutableIntStateOf(0) }

    var dropdownOpen by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf("Choose Puzzle") }
    var message by remember { mutableStateOf<String?>(null) }

    var selectedRow by remember { mutableStateOf<Int?>(null) }
    var selectedCol by remember { mutableStateOf<Int?>(null) }
    var showNumberPicker by remember { mutableStateOf(false) }

    // Timer
    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            elapsed++
        }
    }

    fun loadPuzzle(src: Array<IntArray>, label: String) {
        board = Array(9) { i -> src[i].clone() }
        fixed = Array(9) { r -> BooleanArray(9) { c -> board[r][c] != 0 } }
        selectedDifficulty = label
        elapsed = 0
        mistakes = 0
        hintsUsed = 0
        score = 0
        running = true
    }

    fun clearBoard() {
        board = Array(9) { IntArray(9) { 0 } }
        fixed = Array(9) { BooleanArray(9) { false } }
        elapsed = 0
        mistakes = 0
        hintsUsed = 0
        score = 0
        running = true
        selectedDifficulty = "Custom"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sudoku", fontSize = 26.sp)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("⏱ ${elapsed / 60}:${String.format("%02d", elapsed % 60)}")
            Text("❌ $mistakes")
            Text("💡 ${maxHints - hintsUsed}")
        }
        Spacer(Modifier.height(6.dp))
        Text("🏆 $score", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))

        // Difficulty dropdown
        Box {
            Button(onClick = { dropdownOpen = true }) {
                Text(selectedDifficulty)
            }
            DropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Easy") },
                    onClick = {
                        loadPuzzle(easyPuzzle, "Easy")
                        dropdownOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Medium") },
                    onClick = {
                        loadPuzzle(mediumPuzzle, "Medium")
                        dropdownOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Hard") },
                    onClick = {
                        loadPuzzle(hardPuzzle, "Hard")
                        dropdownOpen = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Clear (Manual)") },
                    onClick = {
                        clearBoard()
                        dropdownOpen = false
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Board
        SudokuGrid(
            board = board,
            fixed = fixed,
            highlightedCell = highlightedCell,
            onCellClick = { r, c ->
                if (!fixed[r][c]) {
                    selectedRow = r
                    selectedCol = c
                    showNumberPicker = true
                }
            }
        )


        Spacer(Modifier.height(12.dp))

        // Control buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                val copy = Array(9) { i -> board[i].clone() }
                if (solveSudoku(copy)) {
                    board = copy
                    running = false
                    score = calculateScore(mistakes, hintsUsed, elapsed)
                    HighFive(messageSetter = { message = it }, score = score, saver = {
                        highScoreManager.saveScore(score)
                    })
                } else {
                    message = "❌ No solution exists."
                }
            }) { Text("Solve") }

            Button(
                onClick = {
                    if (isValidBoard(board)) {
                        message = "✅ Board is valid so far!"
                    } else {
                        mistakes++
                        message = "❌ Invalid board (duplicate in row/col/box)."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
            ) { Text("Validate") }

            val coroutineScope = rememberCoroutineScope()

            Button(
                onClick = {
                    if (hintsUsed >= maxHints) {
                        message = "⚠️ No hints left."
                        return@Button
                    }
                    val pos = getSmartHint(board)
                    if (pos == null) {
                        message = "⚠️ No empty cells or unsolvable."
                        return@Button
                    }
                    val (r, c) = pos
                    val solved = Array(9) { i -> board[i].clone() }
                    if (solveSudoku(solved)) {
                        if (board[r][c] == 0) {
                            board = board.apply { this[r][c] = solved[r][c] }
                            hintsUsed++
                            highlightedCell = Pair(r, c)

                            // Launch coroutine correctly from Composable scope
                            coroutineScope.launch {
                                delay(1000)
                                highlightedCell = null
                            }
                        }
                    }
                },
                enabled = hintsUsed < maxHints,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
            ) { Text("Hint") }

        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { clearBoard() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
            ) { Text("Clear") }

            Button(onClick = onShowLeaderboard,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))) {
                Text("Leaderboard")
            }
        }

        // Messages
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            AlertDialog(
                onDismissRequest = { message = null },
                confirmButton = {
                    TextButton(onClick = { message = null }) { Text("OK") }
                },
                title = { Text("Info") },
                text = { Text(message!!) }
            )
        }
    }

    // Number Picker Dialog
    if (showNumberPicker && selectedRow != null && selectedCol != null) {
        AlertDialog(
            onDismissRequest = { showNumberPicker = false },
            title = { Text("Select a number") },
            text = {
                Column {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 1..3) {
                                val number = row * 3 + col
                                Button(
                                    onClick = {
                                        board = board.mapIndexed { r, rowList ->
                                            rowList.clone().apply {
                                                if (r == selectedRow && col - 1 + row * 3 < 9) {
                                                    this[selectedCol!!] = number
                                                }
                                            }
                                        }.toTypedArray()
                                        showNumberPicker = false
                                    },
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Text(number.toString())
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            board = board.mapIndexed { r, rowList ->
                                rowList.clone().apply {
                                    if (r == selectedRow) this[selectedCol!!] = 0
                                }
                            }.toTypedArray()
                            showNumberPicker = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNumberPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SudokuGrid(
    board: Array<IntArray>,
    fixed: Array<BooleanArray>,
    highlightedCell: Pair<Int, Int>? = null,
    onCellClick: (row: Int, col: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .drawBehind {
                val cell = 40.dp.toPx()
                val thin = 1.dp.toPx()
                val thick = 2.5.dp.toPx()

                drawLine(Color.Black, Offset(0f, 0f), Offset(cell * 9, 0f), thick)
                drawLine(Color.Black, Offset(0f, cell * 9), Offset(cell * 9, cell * 9), thick)
                drawLine(Color.Black, Offset(0f, 0f), Offset(0f, cell * 9), thick)
                drawLine(Color.Black, Offset(cell * 9, 0f), Offset(cell * 9, cell * 9), thick)

                for (i in 1 until 9) {
                    val w = if (i % 3 == 0) thick else thin
                    drawLine(Color.Black, Offset(0f, cell * i), Offset(cell * 9, cell * i), w)
                    drawLine(Color.Black, Offset(cell * i, 0f), Offset(cell * i, cell * 9), w)
                }
            }
            .padding(2.dp)
    ) {
        for (r in 0 until 9) {
            Row {
                for (c in 0 until 9) {
                    val value = board[r][c]
                    val isFixed = fixed[r][c]
                    val bg = when {
                        highlightedCell?.first == r && highlightedCell.second == c -> Color(0xFFFFF176) // yellow highlight
                        isFixed -> Color(0xFFE3F2FD)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(bg)
                            .clickable(enabled = !isFixed) { onCellClick(r, c) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (value == 0) "" else value.toString(),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(highScoreManager: HighScoreManager, onBack: () -> Unit) {
    val scores = remember { mutableStateOf(highScoreManager.getScores()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏆 Leaderboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (scores.value.isEmpty()) {
            Text("No scores yet. Solve a puzzle!")
        } else {
            scores.value.forEachIndexed { i, s ->
                Text("${i + 1}. $s", style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}

private fun HighFive(messageSetter: (String) -> Unit, score: Int, saver: () -> Unit) {
    saver()
    messageSetter("🎉 Puzzle Solved!\n🏆 Score: $score")
}

// Returns the empty cell with fewest possible options
fun getSmartHint(board: Array<IntArray>): Pair<Int, Int>? {
    var bestCell: Pair<Int, Int>? = null
    var minOptions = 10

    for (r in 0 until 9) {
        for (c in 0 until 9) {
            if (board[r][c] != 0) continue
            val options = (1..9).count { isSafe(board, r, c, it) }
            if (options < minOptions) {
                minOptions = options
                bestCell = Pair(r, c)
            }
        }
    }
    return bestCell
}
