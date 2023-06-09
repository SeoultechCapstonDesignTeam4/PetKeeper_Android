package com.example.petkeeper

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService{
    @GET("{userId}")
    fun getUserInfo(@Path("userId") userId: String): Call<UserInfo>

    @POST("/user/login")
    @FormUrlEncoded
    fun postLoginData(
        @Field("USER_EMAIL") user_email: String,
        @Field("USER_PASSWORD")  user_password: String
    ): Call<JsonObject>

    @Multipart
    @POST("/diag/eye")
    fun postEyeImage(
        @Part imageFile: MultipartBody.Part
    ): Call<JsonObject>
}