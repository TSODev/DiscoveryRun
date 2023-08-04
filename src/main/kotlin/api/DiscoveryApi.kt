package api

import com.google.gson.JsonObject
import models.*
import retrofit2.Call
import retrofit2.http.*

interface DiscoveryApi {

    @Headers(
        "Content-Type: application/x-www-form-urlencoded"
    )
    @FormUrlEncoded
    @POST("/api/token")
    fun authenticateUser(
        @Field("grant_type") grant_type: String,
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<AuthentificationResponse>

    @Headers(
        "Content-Type: application/json"
    )
    @POST("events")
    fun apiSendEvent(
        @Body event: JsonObject,

    ): Call<String>

    @Headers(
        "Content-Type: application/json"
    )
    @POST("discovery/runs")
    fun apiCreateRunJob(
        @Body job: JobRun,

        ): Call<JobRunResponse>

    @Headers(
        "Content-Type: application/json"
    )
    @POST("discovery/runs/scheduled")
    fun apiCreateScheduledRunJob(
        @Body job: JobRunScheduled,

        ): Call<Any>

    @Headers(
        "Content-Type: application/json"
    )
    @GET
    fun apiGetFinalResults(@Url url: String): Call<JobFinalResults>

    @Headers(
        "Content-Type: application/json"
    )
    @GET
    fun apiGetInferred(@Url url: String): Call<JobInferredResults>

    @Headers(
        "Content-Type: application/json"
    )
    @GET("discovery/runs")
    fun apiGetJobResults(
        @Query("runId") runId: String,
    ): Call<JobResults>


}