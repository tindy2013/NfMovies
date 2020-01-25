package com.futuretech.nfmovies.Utils


import okhttp3.*

import java.io.IOException
import java.util.concurrent.TimeUnit

class OkHttpUtil private constructor() {
    private val mOkHttpClient: OkHttpClient

    init {
        val ClientBuilder = OkHttpClient.Builder()
        ClientBuilder.readTimeout(READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
        ClientBuilder.writeTimeout(WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
        ClientBuilder.connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.SECONDS)
        mOkHttpClient = ClientBuilder.build()
    }

    fun getHtml(url: String, referer: String): String? {
        var html: String? = null
        val response = instance[url, referer] ?: return null
        try {
            html = response.body!!.string()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return html
    }

    operator fun get(url: String, referer: String): Response? {
        val builder = Request.Builder()
        val request = builder.get().url(url)
                .addHeader("Referer", referer)
                .addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.70 Safari/537.36")
                .build()
        val call = mOkHttpClient.newCall(request)
        var response: Response? = null
        try {
            response = call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return response
    }

    fun post(url: String, params: Map<String, String>, referer: String): Response? {
        val body = setRequestBody(params)
        val builder = Request.Builder()
        val request = builder.post(body).url(url).addHeader("Referer", referer).build()
        val call = mOkHttpClient.newCall(request)
        var response: Response? = null
        try {
            response = call.execute()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return response
    }

    private fun setRequestBody(BodyParams: Map<String, String>?): RequestBody {
        val body: RequestBody?
        val formEncodingBuilder = FormBody.Builder()
        if (BodyParams != null) {
            val iterator = BodyParams.keys.iterator()
            var key : String
            while (iterator.hasNext()) {
                key = iterator.next()
                formEncodingBuilder.add(key, BodyParams.getValue(key))
            }
        }
        body = formEncodingBuilder.build()
        return body

    }

    companion object {
        const val READ_TIMEOUT = 10
        const val CONNECT_TIMEOUT = 10
        const val WRITE_TIMEOUT = 5
        private val mInstance: OkHttpUtil = OkHttpUtil()

        val instance: OkHttpUtil
            @Synchronized get() {
                return mInstance
            }
    }

}
