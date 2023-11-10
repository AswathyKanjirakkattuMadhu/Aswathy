package com.assignment.draughts

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.assignment.draughts.Conditionals.Companion.isFirstSelection

/*
Entry point for UI
 */
private val TAG = "SharedComposables"

@Composable
@Preview
fun DraughtsGame() {
    var currentPlayer by remember {
        mutableStateOf(Color.Red)
    }
    var selectedRow by remember {
        mutableStateOf(-1)
    }
    var selectedCol by remember {
        mutableStateOf(-1)
    }
    var message by remember {
        mutableStateOf("Start with RED player")
    }
    val coins: Array<Array<Box>> by remember {
        mutableStateOf(
            Array(8) { row ->
                Array(8) { col ->
                    val box = Box()
                    box.apply {
                        backgroundColor = if (((row + col) % 2) == 0) {
                            Color.White
                        } else {
                            Color.Black
                        }
                        if (backgroundColor == Color.Black && col != 3 && col != 4) {
                            isCoinPresent = true
                            coinColor = if (col <= 3) {
                                Color.Green
                            } else {
                                Color.Red
                            }
                        }
                    }
                }
            }
        )
    }
    Log.d(TAG, "DraughtsGame: $selectedCol $selectedRow")
    val context = LocalContext.current
    Column {
        Text(text = message)
        GameBoxesCanvas(coins) { col, row ->
            message = "Clicked $row $col"
            Toast.makeText(
                context,
                "`Clicked` $col $row ${coins[col][row].isCoinPresent}",
                Toast.LENGTH_SHORT
            ).show()
            Log.d(TAG, "DraughtsGame: $selectedCol $selectedRow")
            if (isFirstSelection(selectedCol, selectedRow)) {
                if (!coins[col][row].isCoinPresent) {
                    message = "No coin to move"
                } else if (coins[col][row].coinColor != currentPlayer) {
                    message = "Wrong player"
                } else {
                    selectedCol = col
                    selectedRow = row
                    message = "Select target box"
                }
                return@GameBoxesCanvas
            }
            Log.d(TAG, "DraughtsGame: Is Black  = ${(col + row) % 2}")
            if ((col + row) % 2 == 0) {
                return@GameBoxesCanvas
            }

            val selected = coins[selectedCol][selectedRow].coinColor
            coins[selectedCol][selectedRow].coinColor = null
            coins[selectedCol][selectedRow].isCoinPresent = false
            coins[col][row].isCoinPresent = true
            coins[col][row].coinColor = selected
            //Reset the current selection and switch the player Side
            selectedCol = -1
            selectedRow = -1
            currentPlayer = if (currentPlayer == Color.Red) {
                message = "Current Player : Green"
                Color.Green
            } else {
                message = "Current Player : Red"
                Color.Red
            }
        }
    }
}

private fun isValidMove(currentPlayer: Color, selectedCol: Int, col: Int): Boolean {
    if (currentPlayer == Color.Red && selectedCol > col) {
        //Allow movement to top only if the player is red
        return false
    } else if (selectedCol < col) {
        return false
    }
    return true
}

@Composable
fun GameBoxesCanvas(coins: Array<Array<Box>>, onCoinClick: (Int, Int) -> Unit) {
    var canvasSize by remember { mutableStateOf(Offset(0f, 0f)) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    Canvas(
        modifier = Modifier
            .width(screenWidth)
            .height(screenWidth)
            .background(Color.Gray)
            .pointerInput(Unit) {
                detectTapGestures { pan ->
                    val column = (pan.x / (size.width / 8)).toInt()
                    val row = (pan.y / (size.height / 8)).toInt()
                    onCoinClick(column, row)
                }
            }
    ) {
        val canvasWidth = size.width
        val squareSize = canvasWidth / 8f

        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val x = i * squareSize
                val y = j * squareSize
                val squareColor = if ((i + j) % 2 == 0) Color.White else Color.Black

                drawRect(
                    color = squareColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )

                if (coins[i][j].isCoinPresent) {
                    coins[i][j].coinColor?.let {
                        drawCircle(
                            it,
                            center = Offset(x + squareSize / 2, y + squareSize / 2),
                            radius = squareSize / 3,
                        )
                    }
                }
            }
        }
    }
}
