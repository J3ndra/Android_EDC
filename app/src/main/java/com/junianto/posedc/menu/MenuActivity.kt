package com.junianto.posedc.menu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val recyclerView: RecyclerView = findViewById(R.id.menu_rv)
        val layoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = layoutManager

        val buttonList = listOf(
            MenuButton(R.drawable.sale_icon, "SALE", "Card Transaction"),
            MenuButton(R.drawable.settlement_icon, "SETTLEMENTS", "Sync of Transactions"),
            MenuButton(R.drawable.abort_icon, "VOID", "Abort the Transaction"),
            MenuButton(R.drawable.reprint_icon, "REPRINT", "Reprint of Receipt"),
            MenuButton(R.drawable.qris_icon, "QRIS", "Quick Response Code Indonesian Standard"),
            MenuButton(R.drawable.nfc_icon, "E-MONEY", "Electric Money / NFC"),
            MenuButton(R.drawable.settings_icon, "SETTINGS", "Settings parameter of data"),
            MenuButton(R.drawable.about_icon, "ABOUT", "Information of the application")
            // Add more button items as needed
        )

        val adapter = MenuAdapter(buttonList, this)
        recyclerView.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}