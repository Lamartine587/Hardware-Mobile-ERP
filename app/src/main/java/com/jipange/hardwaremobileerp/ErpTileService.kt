package com.jipange.hardwaremobileerp

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ErpTileService : TileService() {

    // This runs when the dropdown menu is pulled down and the tile becomes visible
    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile

        // Ensure the tile is clickable
        if (tile != null) {
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }

    // This runs the exact moment the Manager taps the tile in the dropdown
    override fun onClick() {
        super.onClick()

        // 1. Create an intent to launch your app (we will route it to the Login/Dashboard)
        val intent = Intent(this, MainActivity::class.java).apply {
            // This flag is REQUIRED when launching an app from the background/system menu
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // 2. Collapse the dropdown menu and instantly launch the app
        startActivityAndCollapse(intent)
    }
}