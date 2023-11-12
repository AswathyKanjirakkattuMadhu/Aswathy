package com.assignment.draughts

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
    //Stores the player which is currently playing
    var currentPlayer by remember {
        mutableStateOf(Color.Red)
    }

    //Used selectedRow,selectedCol to store the first selection indexes and
    //to proceed to select second target indexes to move to
    var selectedRow by remember {
        mutableStateOf(-1)
    }
    var selectedCol by remember {
        mutableStateOf(-1)
    }
    var message by remember {
        mutableStateOf("Start with RED player")
    }

    //crossedReds, crossedGreens stores number of coins that has been crossed out
    //which later is used to check whether the player has won by removing all coins
    //and also can be used to show an UI of coins placing outside the board when crossed
    var crossedReds by remember {
        mutableStateOf(0)
    }

    var crossedGreens by remember {
        mutableStateOf(0)
    }

    //2D array which represents the state of the board
    var coins: Array<Array<Box>> by remember {
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
        Text(text = message)
        Button(onClick = {
            currentPlayer = Color.Red
            selectedCol = -1
            selectedRow = -1
            crossedGreens = 0
            crossedReds = 0
            coins = Array(8) { row ->
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
        }) {
            Text(text = "Reset")
        }

        GameBoxesCanvas(coins) { col, row ->
            message = "Selected $row $col, Please select target box"
            var shouldMove = false

            Log.d(TAG, "DraughtsGame: Selected Col $selectedCol Selected Row $selectedRow")
            Log.d(TAG, "DraughtsGame: Target Col $col Target Row $row")
            if (isFirstSelection(selectedCol, selectedRow)) {
                if (!coins[col][row].isCoinPresent) {
                    message = "No coin to move"
                } else if (coins[col][row].coinColor != currentPlayer) {
                    message = "Wrong player"
                } else {
                    selectedCol = col
                    selectedRow = row
                    coins[selectedCol][selectedRow].backgroundColor = Color.DarkGray
                    message = "Select target box"
                }
                return@GameBoxesCanvas
            }

            if ((col + row) % 2 == 0) {
                message = "Can only move to blacks!"
                return@GameBoxesCanvas
            }

            /*
            If the coin is king, passing the both red and green conditions,
            it will allow the coin to move to any of the side as
            red moves up and green moves down, so we can reuse the logic to avoid
            code duplication
             */
            if (currentPlayer == Color.Red || coins[selectedCol][selectedRow].isKing) {
                if (!isValidRedMove(
                        selectedRow,
                        row,
                        selectedCol,
                        col,
                        coins
                    ) && !coins[selectedCol][selectedRow].isKing
                ) {
                    message = "Invalid move"
                    return@GameBoxesCanvas
                }

                shouldMove = !coins[col][row].isCoinPresent
                if (selectedCol - 1 == col && selectedRow - 1 == row) {
                    //trying to move left diagonal
                    if (coins[selectedCol - 1][selectedRow - 1].isCoinPresent) {
                        message = "Already occupied"
                        //Deny the move
                        return@GameBoxesCanvas
                    }
                }
                if (selectedCol + 1 == col && selectedRow + 1 == row) {
                    //trying to move left diagonal
                    if (coins[selectedCol - 1][selectedRow - 1].isCoinPresent) {
                        message = "Already occupied"
                        //Deny the move
                        return@GameBoxesCanvas
                    }
                }
                if (selectedCol - 2 == col && selectedRow - 2 == row) {
                    //Trying to cross left diagonal coin
                    if (coins[selectedCol - 1][selectedRow - 1].isCoinPresent && !coins[col][row].isCoinPresent) {
                        //There is a coin present in between
                        if (coins[selectedCol - 1][selectedRow - 1].coinColor == Color.Green || coins[selectedCol][selectedRow].isKing) {
                            //The coin in between is green
                            //Remove the green coin
                            crossedGreens++
                            coins[selectedCol - 1][selectedRow - 1].coinColor = null
                            coins[selectedCol - 1][selectedRow - 1].isCoinPresent = false
                            //Proceed to move the coin
                            shouldMove = true
                        }
                    } else {
                        message =
                            "Cannot make move, either target is not empty or in between is not occupied"
                    }
                }
                if (selectedCol + 2 == col && selectedRow - 2 == row) {
                    //Trying to cross left diagonal coin
                    if (coins[selectedCol + 1][selectedRow - 1].isCoinPresent && !coins[col][row].isCoinPresent) {
                        //There is a coin present in between
                        if (coins[selectedCol + 1][selectedRow - 1].coinColor == Color.Green || coins[selectedCol][selectedRow].isKing) {
                            //The coin in between is green
                            //Remove the green coin
                            crossedGreens++
                            coins[selectedCol + 1][selectedRow - 1].coinColor = null
                            coins[selectedCol + 1][selectedRow - 1].isCoinPresent = false
                            //Proceed to move the coin
                            shouldMove = true
                        }
                    } else {
                        message =
                            "Cannot make move, either target is not empty or in between is not occupied"
                    }
                }
                if (row == 0 && shouldMove) {
                    //King the red player as it has reached the top
                    coins[selectedCol][selectedRow].isKing = true
                }
            }
            /*
             If the coin is king, passing the both red and green conditions,
             it will allow the coin to move to any of the side as
             red moves up and green moves down, so we can reuse the logic to avoid
             code duplication
             */
            if (currentPlayer == Color.Green || coins[selectedCol][selectedRow].isKing) {
                if (!coins[col][row].isCoinPresent) {
                    if (isValidGreenMove(
                            selectedRow,
                            row,
                            selectedCol,
                            col,
                            coins
                        ) && !coins[selectedCol][selectedRow].isKing
                    ) {
                        Log.d(TAG, "DraughtsGame: Valid green move ")
                        shouldMove = true
                    }
                }
                if (selectedCol + 1 == col && selectedRow + 1 == row) {
                    //trying to move left diagonal
                    if (coins[selectedCol + 1][selectedRow + 1].isCoinPresent) {
                        message = "Already occupied"
                        //Deny the move
                        shouldMove = false
                    }
                }
                if (selectedCol - 2 == col && selectedRow + 2 == row && !coins[col][row].isCoinPresent) {
                    //Trying to cross bottom right diagonal coin
                    if (coins[selectedCol - 1][selectedRow + 1].isCoinPresent) {
                        //There is a coin present in between
                        if (coins[selectedCol - 1][selectedRow + 1].coinColor == Color.Red || coins[selectedCol][selectedRow].isKing) {
                            //The coin in between is red
                            //Remove the red coin
                            crossedReds++
                            coins[selectedCol - 1][selectedRow + 1].coinColor = null
                            coins[selectedCol - 1][selectedRow + 1].isCoinPresent = false
                            //Proceed to move the coin
                            shouldMove = true
                        }
                    }
                } else {
                    message =
                        "Cannot make move, either target is not empty or in between is not occupied"
                }
                if (selectedCol + 2 == col && selectedRow + 2 == row && !coins[col][row].isCoinPresent) {
                    //Trying to cross left diagonal coin
                    if (coins[selectedCol + 1][selectedRow + 1].isCoinPresent && !coins[col][row].isCoinPresent) {
                        //There is a coin present in between
                        if (coins[selectedCol + 1][selectedRow + 1].coinColor == Color.Red || coins[selectedCol][selectedRow].isKing) {
                            //The coin in between is red
                            //Remove the red coin
                            crossedReds++
                            coins[selectedCol + 1][selectedRow + 1].coinColor = null
                            coins[selectedCol + 1][selectedRow + 1].isCoinPresent = false
                            //Proceed to move the coin
                            shouldMove = true
                        }
                    }
                } else {
                    message =
                        "Cannot make move, either target is not empty or in between is not occupied"
                }

                if (row == 7 && shouldMove) {
                    //King the green player as it has reached the bottom
                    coins[selectedCol][selectedRow].isKing = true
                }

            }

            if (shouldMove) {
                //Make the swap of coin from the selected row-col to target row-col
                val selected = coins[selectedCol][selectedRow].coinColor
                coins[col][row].isKing = coins[selectedCol][selectedRow].isKing
                coins[selectedCol][selectedRow].coinColor = null
                coins[selectedCol][selectedRow].isCoinPresent = false
                coins[col][row].isCoinPresent = true
                coins[col][row].coinColor = selected
                //Make the background back to black otherwise the grey will stay
                coins[selectedCol][selectedRow].backgroundColor = Color.Black
                //Reset the selection
                selectedCol = -1
                selectedRow = -1
                //Switch the player so the turns are alternated
                currentPlayer = if (currentPlayer == Color.Red) {
                    message = "Current Player : Green"
                    Color.Green
                } else {
                    message = "Current Player : Red"
                    Color.Red
                }
            }
        }
        //Show a list of coins which denotes the number of coins crossed
        CrossedOutCoinsView(count = crossedReds, coinColor = Color.Red)
        CrossedOutCoinsView(count = crossedGreens, coinColor = Color.Green)
        //Show a cancel selection button when a coin is slected and a move has not been
        //performed yet. This will allow user to have a good UX
        if (selectedCol != -1 && selectedRow != -1) {
            Button(onClick = {
                coins[selectedCol][selectedRow].backgroundColor = Color.Black
                selectedCol = -1
                selectedRow = -1
            }) {
                Text(text = "Cancel Selection")
            }
        }
        //12 Reds has been crossed, green won the game
        if (crossedReds == 12) {
            ShowGameWonAlert(player = "Green")
        }
        //12 Greens has been crossed, green won the game
        if (crossedGreens == 12) {
            ShowGameWonAlert(player = "Red")
        }
    }
}


//Show alert box that the player has won the match
@Composable
fun ShowGameWonAlert(player: String) {
    var isFinished: Boolean by remember {
        mutableStateOf(true)
    }
    if (isFinished) {
        AlertDialog(onDismissRequest = {

        }, confirmButton = {
            Button(onClick = { isFinished = false }) {
                Text(text = "Done")
            }
        },
            title = { Text(text = "$player has won the match") }
        )
    }
}

//Check the movement of a RED coin is valid,
//this validates the movement to first diagonal box or to the second diagonal box if crossing is possible
fun isValidRedMove(
    selectedRow: Int,
    row: Int,
    selectedCol: Int,
    col: Int,
    coins: Array<Array<Box>>
): Boolean {
    return (selectedCol - 1 == col && selectedRow - 1 == row) ||
            (selectedCol + 1 == col && selectedRow - 1 == row) ||
            (selectedCol - 2 == col && selectedRow - 2 == row && coins[selectedCol - 1][selectedRow - 1].isCoinPresent) ||
            (selectedCol + 2 == col && selectedRow - 2 == row && coins[selectedCol + 1][selectedRow - 1].isCoinPresent)
}

//Check the movement of a GREEN coin is valid,
//this validates the movement to first diagonal box or to the second diagonal box if crossing is possible
fun isValidGreenMove(
    selectedRow: Int,
    row: Int,
    selectedCol: Int,
    col: Int,
    coins: Array<Array<Box>>
): Boolean {
    return (selectedCol + 1 == col && selectedRow + 1 == row) ||
            (selectedCol - 1 == col && selectedRow + 1 == row) ||
            (selectedCol + 2 == col && selectedRow - 2 == row && coins[selectedCol + 1][selectedRow - 1].isCoinPresent) ||
            (selectedCol + 2 == col && selectedRow + 2 == row && coins[selectedCol + 1][selectedRow + 1].isCoinPresent)
}


//Main board canvas which shows the whites and black boxes and with coins
@Composable
fun GameBoxesCanvas(coins: Array<Array<Box>>, onCoinClick: (Int, Int) -> Unit) {
    var canvasSize by remember { mutableStateOf(Offset(0f, 0f)) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    //Usage of single canvas and dividing them up to create mini boxes
    Canvas(
        modifier = Modifier
            .width(screenWidth)
            .height(screenWidth)
            .background(Color.Gray)
            .pointerInput(Unit) {
                detectTapGestures { pan ->
                    //To detect the clicks for moving coins
                    val column = (pan.x / (size.width / 8)).toInt()
                    val row = (pan.y / (size.height / 8)).toInt()
                    onCoinClick(column, row)
                }
            }
    ) {
        val canvasWidth = size.width
        val squareSize = canvasWidth / 8f
        //Fill up 8x8 boxes to UI
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val x = i * squareSize
                val y = j * squareSize

                //Draw the background as white or black. If the position is selected to
                //move then the color would be grey to highlight the selection
                drawRect(
                    color = coins[i][j].backgroundColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(squareSize, squareSize)
                )
                //Draw a coin if the position is containing a coin with the coin color
                if (coins[i][j].isCoinPresent) {
                    coins[i][j].coinColor?.let {
                        drawCircle(
                            it,
                            center = Offset(x + squareSize / 2, y + squareSize / 2),
                            radius = squareSize / 3,
                        )
                    }
                    //If the coin is king then denote the king with a mini circle inside it
                    if (coins[i][j].isKing) {
                        drawCircle(
                            Color.DarkGray,
                            center = Offset(x + squareSize / 2, y + squareSize / 2),
                            radius = squareSize / 5,
                        )
                    }
                }
            }
        }
    }
}



