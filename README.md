🧩 Sudoku Solver & Player (Kotlin + Jetpack Compose)

A modern Sudoku Android app built with Kotlin and Jetpack Compose, featuring:

✨ Features

🎮 Multiple Difficulty Levels – Easy, Medium, Hard puzzles that change every time you play.

🖱️ Interactive Grid – Tap a cell and select numbers (1–9) via a number picker dialog.

💡 Hints System – Get up to 3 hints per game (only for empty, non-prefilled cells).

⚡ Scoring System – Gain points for correct entries, lose points for mistakes.

🏆 Leaderboard – Top 5 scores are saved locally using SharedPreferences.

🌗 Light/Dark Mode Support – Adapts to system theme with Material 3.

🔄 Puzzle Randomization – Each difficulty level selects a random puzzle from a set.

🛠️ Solver – Includes a backtracking-based Sudoku solver for instant solutions.

🛠️ Tech Stack

Language: Kotlin

UI: Jetpack Compose + Material 3

Data Persistence: SharedPreferences + Gson

Algorithm: Backtracking Sudoku Solver

📱 How It Works

Select a difficulty to load a fresh Sudoku puzzle.

Tap any empty cell → pick a number (1–9).

Use Hints (max 3) if stuck.

Wrong entries deduct points, correct ones improve score.

When puzzle is solved → score is saved to leaderboard.

🚀 Future Enhancements

Add timer mode for speed-based scoring.

Support custom puzzle input.

Add undo/redo functionality.

Online global leaderboard.
