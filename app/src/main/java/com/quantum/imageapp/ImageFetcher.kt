package com.quantum.imageapp
import android.os.AsyncTask
import org.json.JSONArray
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
//ImageFetcher.kt
class ImageFetcher(private val listener: Listener) {
    private var currentPage = 1
    private val perPage = 30 // Number of images per page
    private val maxImages = 100 // Maximum number of images to fetch
  //  https://api.unsplash.com/photos/?client_id=Tdni4SXCriJZcdBEdexKeKyTUNaLopZoBzSfVz_pUqM&page=1&per_page=30
    private val baseUrl = "https://api.unsplash.com/photos"
    private val clientId = "Tdni4SXCriJZcdBEdexKeKyTUNaLopZoBzSfVz_pUqM" // Replace with your Unsplash client ID

    interface Listener {
        fun onImageUrlsLoaded(urls: List<String>)
        fun onError(e: Exception)
    }

    inner class FetchImageUrlsTask : AsyncTask<Void, Void, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            val imageUrls = mutableListOf<String>()
            var inputStream: InputStream? = null
            var urlConnection: HttpURLConnection? = null

            try {
                val url = URL(buildUrl())
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 3000
                urlConnection.readTimeout = 3000
                urlConnection.requestMethod = "GET"
                urlConnection.doInput = true
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP error code: $responseCode")
                }

                inputStream = urlConnection.inputStream
                if (inputStream != null) {
                    val jsonString = inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)
                    for (i in 0 until jsonArray.length()) {
                        if (imageUrls.size >= maxImages) {
                            break // Stop fetching once maxImages limit is reached
                        }
                        val photoObject = jsonArray.getJSONObject(i)
                        val regularUrl = photoObject.getJSONObject("urls").getString("regular")
                        imageUrls.add(regularUrl)
                    }
                }
            } catch (e: Exception) {
                listener.onError(e)
            } finally {
                inputStream?.close()
                urlConnection?.disconnect()
            }

            return imageUrls
        }

        override fun onPostExecute(result: List<String>) {
            super.onPostExecute(result)
            listener.onImageUrlsLoaded(result)
        }
    }

    fun fetchImageUrls() {
        FetchImageUrlsTask().execute()
    }

    private fun buildUrl(): String {
        return "$baseUrl/?client_id=$clientId&page=$currentPage&per_page=$perPage"
    }
}
