package com.assignment.draughts

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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
    Log.d(TAG, "DraughtsGame: Col $selectedCol Row $selectedRow")
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
    Column {
        if (message.isNotBlank()) {
            Text(text = message)
        }
        Column {
            repeat(8) { col ->
                Row {
                    repeat(8) { row ->
                        val box = coins[row][col]
                        SingleBox(box) {
                            message = ""
                            if (box.coinColor != currentPlayer && isFirstSelection(selectedCol,selectedRow)) {
                                message = "Wrong player selected"
                                return@SingleBox
                            }
                            //Remember the selected row and col as the selection is the from box
                            if (isFirstSelection(selectedCol, selectedRow)) {
                                selectedRow = row
                                selectedCol = col
                                message = "Selected Col: $selectedCol Row: $selectedRow"
                            } else {
                                if ((row == selectedRow && col == selectedCol) || (box.backgroundColor != Color.Black)) {
                                    //Same box selection
                                    return@SingleBox
                                } else if (box.isCoinPresent) {
                                    //Proceed to move the coin as the selection is now to the target box
                                    message = "Cannot move to filled boxes"
                                } else {
                                    //Ready to move the coin
                                    //Check the coin is in black target
                                    val selected = coins[selectedRow][selectedCol].coinColor
                                    coins[selectedRow][selectedCol].coinColor = null
                                    coins[selectedRow][selectedCol].isCoinPresent = false
                                    coins[row][col].isCoinPresent = true
                                    coins[row][col].coinColor = selected
                                    //Reset the current selection and switch the player Side
                                    selectedCol = -1
                                    selectedRow = -1
                                    currentPlayer =
                                        if (currentPlayer == Color.Red) Color.Green else Color.Red
                                    message = "Player $currentPlayer turn"
                                }
                                message =
                                    "Selected to move from Col: $selectedCol Row: $selectedRow to Col: $col Row: $row"
                            }

                            Log.d(TAG, "DraughtsGame: Col $selectedCol Row $selectedRow")

                        }
                    }
                }
            }
        }
    }
}

private fun isFirstSelection(selectedCol: Int, selectedRow: Int) =
    selectedCol == -1 && selectedRow == -1


@Composable
fun SingleBox(
    boxData: Box,
    clickAction: () -> Unit
) {
    Log.d(TAG, "SingleBox: ${boxData.isCoinPresent} ${boxData.coinColor == Color.Red}")
    Box(
        modifier = Modifier
            .background(boxData.backgroundColor)
            .size(40.dp)
            .clickable {
                clickAction()
            }
    ) {
        if (boxData.isCoinPresent) {
            Canvas(modifier = Modifier.matchParentSize()) {
                boxData.coinColor?.let {
                    drawCircle(
                        it,
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width / 3
                    )
                }
            }
        }
    }
}