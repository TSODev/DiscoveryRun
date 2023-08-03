package network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class oldRetrofitClient(serverUrl: String) {


    companion object {
//        private val gson = GsonBuilder()
//            //       .setLenient()
//            .create()
        val LoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        private val okHttpClient = OkHttpClient()
            .newBuilder()
//            .addInterceptor(LoggingInterceptor)
            .addInterceptor(RequestInterceptor)
            .build()


        fun getClient(serverUrl: String): Retrofit =
            Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        }
    }
