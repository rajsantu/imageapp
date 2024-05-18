package com.quantum.imageapp


import android.os.Bundle
import android.util.Log

import android.widget.GridView

import androidx.appcompat.app.AppCompatActivity

// MainActivity.kt
class MainActivity : AppCompatActivity(), ImageFetcher.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imageFetcher = ImageFetcher(this)
        imageFetcher.fetchImageUrls()
    }

    override fun onImageUrlsLoaded(urls: List<String>) {
        val gridView = findViewById<GridView>(R.id.grid_view)
        gridView.adapter = ImageAdapter(this, urls)
    }

    override fun onError(e: Exception) {
        Log.e("MainActivity", "Error fetching image URLs", e)
    }
}

