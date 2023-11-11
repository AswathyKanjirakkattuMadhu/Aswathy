package com.assignment.draughts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp


/*
A view to show a list of coins when it has been crossed out
count: How many coins should be displayed
coinColor : Color of the coin
 */
@Composable
fun CrossedOutCoinsView(count: Int, coinColor: Color) {

    Row {
        for (i in 0 until count) {
            Coin(coinColor = coinColor)
        }
    }
}

@Composable
fun Coin(coinColor: Color) {
    Canvas(
        modifier = Modifier
            .size(40.dp)
            .padding(5.dp)
    ) {
        drawCircle(
            color = coinColor,
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width / 2f
        )
    }
}
