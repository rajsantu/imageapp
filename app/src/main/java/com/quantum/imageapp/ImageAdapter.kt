package com.quantum.imageapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.graphics.Bitmap
import android.os.AsyncTask
import android.graphics.BitmapFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
//ImageAdapter.kt
class ImageAdapter(context: Context, private val imageUrls: List<String>) :
    ArrayAdapter<String>(context, 0, imageUrls) {
    private val bitmapCache = BitmapCache(context)
    private var currentTasks = mutableMapOf<Int, LoadImageTask>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView = if (convertView == null) {
            ImageView(context).apply {
                layoutParams = ViewGroup.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        } else {
            convertView as ImageView
        }

        // Cancel any ongoing task for this position
        currentTasks[position]?.cancel(true)

        val imageUrl = getItem(position)
        val bitmap = imageUrl?.let { bitmapCache.getBitmapFromCache(it) }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
        } else {
            val loadImageTask = LoadImageTask(imageView, position)
            currentTasks[position] = loadImageTask
            loadImageTask.execute(imageUrl)
        }

        return imageView
    }

    private inner class LoadImageTask(private val imageView: ImageView, private val position: Int) :
        AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val urlConnection = URL(urls[0]).openConnection() as HttpURLConnection
            urlConnection.connectTimeout = 3000
            urlConnection.readTimeout = 3000
            urlConnection.requestMethod = "GET"
            urlConnection.doInput = true
            urlConnection.connect()

            val responseCode = urlConnection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error code: $responseCode")
            }

            if (isCancelled) {
                return null
            }

            val inputStream: InputStream = urlConnection.inputStream
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            urlConnection.disconnect()

            bitmapCache.addBitmapToCache(urls[0], bitmap)

            return bitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                imageView.setImageBitmap(result)
            }
            // Remove completed task from the map
            currentTasks.remove(position)
        }

        override fun onCancelled(result: Bitmap?) {
            // Remove cancelled task from the map
            currentTasks.remove(position)
        }
    }
}

