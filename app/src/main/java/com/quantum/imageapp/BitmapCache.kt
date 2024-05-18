package com.quantum.imageapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import android.os.AsyncTask
import android.util.Log

//BitmapCache.kt


class BitmapCache(context: Context) {
    private val memoryCache: LruCache<String, Bitmap>
    private val diskCacheDir: File = context.cacheDir

    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    fun getBitmapFromCache(key: String): Bitmap? {
        var bitmap: Bitmap? = memoryCache.get(key)

        if (bitmap == null) {
            bitmap = getBitmapFromDiskCache(key)
            if (bitmap != null) {
                // If found in disk cache, update memory cache
                memoryCache.put(key, bitmap)
            }
        }

        return bitmap
    }

    fun addBitmapToCache(key: String, bitmap: Bitmap?) {
        if (bitmap != null) {
            memoryCache.put(key, bitmap)
            AddToDiskCacheTask(diskCacheDir, key).execute(bitmap)
        }
    }

    private fun getBitmapFromDiskCache(key: String): Bitmap? {
        val file = File(diskCacheDir, key.hashCode().toString())
        if (file.exists()) {
            val inputStream = FileInputStream(file)
            return BitmapFactory.decodeStream(inputStream)
        }
        return null
    }

    private class AddToDiskCacheTask(private val cacheDir: File, private val key: String) :
        AsyncTask<Bitmap, Void, Void>() {
        override fun doInBackground(vararg bitmaps: Bitmap): Void? {
            val bitmap = bitmaps[0]
            val file = File(cacheDir, key.hashCode().toString())
            try {
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("BitmapCache", "Error AddToDiskCacheTask", e)
            }
            return null
        }
    }
}
