package com.assignment.draughts

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.assignment.draughts.Conditionals.Companion.isFirstSelection

/*
Entry point for UI
 */
private val TAG = "SharedComposables"
private val SETTINGS = "settings"
private val PLAYER_TOP = "playertop"
private val PLAYER_BOTTOM = "playerbottom"
private val BACKGROUND_DARK = "darkbackground"
private val BACKGROUND_LIGHT = "lightbackground"


@Composable
@Preview
fun DraughtsGame() {
    //Stores the player which is currently playing

    val context = LocalContext.current

    val topPlayerColor by remember {
        mutableStateOf(getTopPlayerColor(context))
    }

    val bottomPlayerColor by remember {
        mutableStateOf(getBottomPlayerColor(context))
    }
    var currentPlayer by remember {
        mutableStateOf(topPlayerColor)
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

    var settingsAlert by remember {
        mutableStateOf(false)
    }

    var selectedAlertOption by remember {
        mutableStateOf("")
    }

    var selectedColorInt by remember {
        mutableStateOf(0)
    }

    //2D array which represents the state of the board
    var coins: Array<Array<Box>> by remember {
        mutableStateOf(initializeCoins(context))
    }

    Column {
        Text(text = message)
        Row {
            Button(onClick = {
                currentPlayer = topPlayerColor
                selectedCol = -1
                selectedRow = -1
                crossedGreens = 0
                crossedReds = 0
                coins = Array(8) { row ->
                    Array(8) { col ->
                        val box = Box()
                        box.apply {
                            backgroundColor = if (((row + col) % 2) == 0) {
                                getLightBackgroundColor(context)
                            } else {
                                getDarkBackgroundColor(context)
                            }
                            if (backgroundColor == Color.Black && col != 3 && col != 4) {
                                isCoinPresent = true
                                coinColor = if (col <= 3) {
                                    bottomPlayerColor
                                } else {
                                    topPlayerColor
                                }
                            }
                        }
                    }
                }
            }) {
                Text(text = "Reset")
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = { settingsAlert = true }) {
                Text(text = "Settings")
            }
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
            if (currentPlayer == topPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
                        if (coins[selectedCol - 1][selectedRow - 1].coinColor == bottomPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
                        if (coins[selectedCol + 1][selectedRow - 1].coinColor == bottomPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
            if (currentPlayer == bottomPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
                        if (coins[selectedCol - 1][selectedRow + 1].coinColor == topPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
                        if (coins[selectedCol + 1][selectedRow + 1].coinColor == topPlayerColor || coins[selectedCol][selectedRow].isKing) {
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
                currentPlayer = if (currentPlayer == topPlayerColor) {
                    message = "Current Player : Top"
                    bottomPlayerColor
                } else {
                    message = "Current Player : Bottom"
                    topPlayerColor
                }
            }
        }
        //Show a list of coins which denotes the number of coins crossed
        CrossedOutCoinsView(count = crossedReds, coinColor = topPlayerColor)
        CrossedOutCoinsView(count = crossedGreens, coinColor = bottomPlayerColor)
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
        //12 top has been crossed, bottom won the game
        if (isPlayerEmpty(coins, topPlayerColor)) {
            crossedGreens = 0
            crossedReds = 0
            var isFinished: Boolean by remember {
                mutableStateOf(true)
            }
            if (isFinished) {
                AlertDialog(onDismissRequest = {

                }, confirmButton = {
                    Button(onClick = {
                        isFinished = false
                        coins = initializeCoins(context)
                    }) {
                        Text(text = "Done")
                        //  coins = initializeCoins(context)
                    }
                },
                    title = { Text(text = "Bottom Player has won the match") }
                )
            }
        }
        //12 bottom has been crossed, top won the game
        if (isPlayerEmpty(coins, bottomPlayerColor)) {
            crossedGreens = 0
            crossedReds = 0
            var isFinished = true
            if (isFinished) {
                AlertDialog(onDismissRequest = {

                }, confirmButton = {
                    Button(onClick = {
                        isFinished = false
                        coins = initializeCoins(context)
                    }
                    ) {
                        Text(text = "Done")

                    }
                },
                    title = { Text(text = "Top Player has won the match") }
                )
            }
        }

        if (settingsAlert) {
            var showSlider by remember {
                mutableStateOf(false)
            }
            Dialog(onDismissRequest = {
                settingsAlert = false
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(400.dp)
                        .border(
                            border = BorderStroke(
                                10.dp, color = Color(
                                    android.graphics.Color.parseColor(
                                        String.format(
                                            "#%06X",
                                            0xFFFFFF and selectedColorInt
                                        )
                                    )
                                )
                            )
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Top Player Color")
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    selectedAlertOption = PLAYER_TOP
                                    showSlider = true
                                },
                                colors = ButtonDefaults.buttonColors(getTopPlayerColor(context))
                            ) {
                                Text(text = "Edit")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Bottom Player Color")
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    selectedAlertOption = PLAYER_BOTTOM
                                    showSlider = true
                                },
                                colors = ButtonDefaults.buttonColors(getBottomPlayerColor(context))
                            ) {
                                Text(text = "Edit")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Dark Background Color")
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    selectedAlertOption = BACKGROUND_DARK
                                    showSlider = true
                                },
                                colors = ButtonDefaults.buttonColors(getDarkBackgroundColor(context))
                            ) {
                                Text(text = "Edit")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Light Background Color")
                            Spacer(Modifier.weight(1f))
                            Button(
                                onClick = {
                                    selectedAlertOption = BACKGROUND_LIGHT
                                    showSlider = true
                                },
                                colors = ButtonDefaults.buttonColors(getLightBackgroundColor(context))
                            ) {
                                Text(text = "Edit")
                            }
                        }

                        Slider(
                            value = selectedColorInt.toFloat(),
                            onValueChange = {
                                Log.d(TAG, "DraughtsGame: $it")
                                selectedColorInt = it.toInt()
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            steps = 100,
                            valueRange = 0f..50000f
                        )
                        Button(
                            onClick = {
                                saveColorToStorage(
                                    selectedColorInt,
                                    selectedAlertOption,
                                    context
                                )
                                coins = initializeCoins(context)
                            }, modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(8.dp)
                        ) {
                            Text(text = if (selectedAlertOption.isEmpty()) "Select option to edit" else "Save $selectedAlertOption")
                        }
                        Button(
                            onClick = {
                                resetColors(context)
                                coins = initializeCoins(context)
                            }, modifier = Modifier
                                .height(50.dp)
                                .padding(8.dp)
                        ) {
                            Text(text = "Reset")
                        }
                    }

                }
            }
        }
    }

}

fun isPlayerEmpty(coins: Array<Array<Box>>, topPlayerColor: Color): Boolean {
    var count = 0
    coins.iterator().forEachRemaining {
        it.iterator().forEachRemaining { it2 ->
            if (it2.coinColor != null) {
                if (it2.coinColor == topPlayerColor) {
                    count++
                }
            }
        }
    }
    return count == 11
}

fun getDarkBackgroundColor(context: Context): Color {
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val colorString = prefs.getString(BACKGROUND_DARK, "#000000")
    return Color(android.graphics.Color.parseColor(colorString))
}

fun getLightBackgroundColor(context: Context): Color {
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val colorString = prefs.getString(BACKGROUND_LIGHT, "#ffffff")
    return Color(android.graphics.Color.parseColor(colorString))
}

private fun initializeCoins(context: Context): Array<Array<Box>> {
    var coins: Array<Array<Box>> =
        Array(8) { row ->
            Array(8) { col ->
                val box = Box()
                box.apply {
                    backgroundColor = if (((row + col) % 2) == 0) {
                        getLightBackgroundColor(context)
                    } else {
                        getDarkBackgroundColor(context)
                    }
                    if (backgroundColor == Color.Black && col != 3 && col != 4) {
                        isCoinPresent = true
                        coinColor = if (col <= 3) {
                            getBottomPlayerColor(context)
                        } else {
                            getTopPlayerColor(context)
                        }
                    }
                }
            }
        }
    return coins
}

fun saveColorToStorage(selectedColorInt: Int, selectedAlertOption: String, context: Context) {
    Log.d(TAG, "saveColorToStorage: $selectedAlertOption")
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val editor = prefs.edit()
    val hex = String.format("#%06X", 0xFFFFFF and selectedColorInt)
    editor.putString(selectedAlertOption, hex)
    editor.apply()
}

fun resetColors(context: Context) {
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val editor = prefs.edit()
    editor.putString(BACKGROUND_DARK, "#000000")
    editor.putString(BACKGROUND_LIGHT, "#ffffff")
    editor.putString(PLAYER_TOP, "#b3003e")
    editor.putString(PLAYER_BOTTOM, "#078205")
    editor.apply()
}

fun getBottomPlayerColor(context: Context): Color {
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val colorString = prefs.getString(PLAYER_TOP, "#b0281e")
    return Color(android.graphics.Color.parseColor(colorString))
}

fun getTopPlayerColor(context: Context): Color {
    val prefs = context.getSharedPreferences(SETTINGS, MODE_PRIVATE)
    val colorString = prefs.getString(PLAYER_BOTTOM, "#1fbfb5")
    return Color(android.graphics.Color.parseColor(colorString))
}


//Show alert box that the player has won the match
@Composable
fun ShowGameWonAlert(player: String) {

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



