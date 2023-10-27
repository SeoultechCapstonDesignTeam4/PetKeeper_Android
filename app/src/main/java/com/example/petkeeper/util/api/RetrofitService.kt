package com.example.petkeeper.util.api

import com.example.petkeeper.model.CommunityResponse
import com.example.petkeeper.model.HospitalResponse
import com.example.petkeeper.model.LoginResponse
import com.example.petkeeper.model.UpdatePetBody
import com.example.petkeeper.model.User
import com.example.petkeeper.model.UserResponse
import com.example.petkeeper.util.App
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService{
    @GET("/user/{userId}")
    fun getUserInfo(
        @Header("Authorization") token: String? = "Bearer ${App.preferences.token}",
        @Path("userId") userId: String
    ): Call<UserResponse>

    //로그인 Todo-SharedPreference token, USER 저장하기 && USER.p_pets의 length가 0이면 강아지 정보등록 페이지로 이동, 1이상이면 메인페이지로
    @POST("/user/login")
    @FormUrlEncoded
    fun postLoginData(
        @Field("USER_EMAIL") email: String,
        @Field("USER_PASSWORD")  password: String
    ): Call<LoginResponse>

    @GET("/hospital")
    fun getHospital(
        @Query("X") x: String,
        @Query("Y") y: String
    ): Call<HospitalResponse>

    //로그아웃 Todo-API 날리고 SharedPreference 날리기
    @GET("/user/logout")
    fun userLogOut(
        @Header("Authorization") token: String? = "Bearer ${App.preferences.token}"
    ): Call <JsonObject>

    //회원 정보 수정 Todo-SharedPreference 날리고 token, USER 다시 받기
    @FormUrlEncoded
    @PUT("/user/{user_id}")
    fun updateUser(
        @Header("Authorization") token: String? = "Bearer ${App.preferences.token}",
        //Todo-USER_ID 값 같이 보내기
        @Path("user_id") id: String? = App.preferences.userId.toString(),
        @Field("USER_EMAIL") USER_EMAIL: String?,
        @Field("USER_PASSWORD") USER_PASSWORD: String?,
        @Field("USER_PHONE") USER_PHONE: String?,
    ): Call<JsonObject>

    //회원 정보 삭제
    @DELETE("/user/{user_id}")
    fun deleteUser(
        @Header("Authorization") token: String? = App.preferences.token,
        //Todo-USER_ID 값 같이 보내기
//        @Path("user_id") id: String? = App.preferences
    ): Call<JsonObject>

    //회원 이메일 잃어버림
    @FormUrlEncoded
    @POST("/user/forget/email")
    fun userEmailFind(
        @Field("USER_PHONE") USER_PHONE: String,
        @Field("USER_NAME") USER_NAME: String
    ):Call <JsonObject>

    //회원 비밀번호 잃어버림
    @FormUrlEncoded
    @POST("/user/forget/password")
    fun userPasswordFind(
        @Field("USER_PHONE") USER_PHONE: String,
        @Field("USER_NAME") USER_NAME: String,
        @Field("USER_EMAIL") USER_EMAIL: String,
    ):Call <JsonObject>

    //회원 이미지 삭제 -> default_img 로 바뀜
    @DELETE("/user/user-img/{user_id}")
    fun deleteUserImage(
        @Header("Authorization") token: String? = App.preferences.token,
        //Todo-아래에 User_ID 추가하기
//        @Path("user_id") id: String? = App.preferences.User().id,
    ): Call<JsonObject>

    //Todo-이미지 첨부할때는 imageFile -> image 로 필드명 고쳐줄 것
    @Multipart
    @POST("/diag/eye")
    fun postEyeImage(
        @Header("Authorization") token: String? = "Bearer ${App.preferences.token}",
        @Part image: MultipartBody.Part
    ): Call<JsonObject>

    @FormUrlEncoded
    @POST("/pet")
    fun postPetData(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Field("PET_NAME") PET_NAME: String,
        @Field ("PET_KIND") PET_KIND: String,
        @Field ("PET_GENDER") PET_GENDER: String,
        @Field ("PET_BIRTHDATE") PET_BIRTHDATE: String
    ): Call<JsonObject>

    //  펫 정보 수정 Todo-SharedPreference 날리고 token, USER 다시 받기
    @Multipart
    @PUT("/pet/{pet_id}")
    fun updatePet(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("pet_id") id: String? = App.preferences.Pet().id,
        @PartMap pet: HashMap<String, RequestBody>
    ):Call<JsonObject>

    //  펫 삭제
    @DELETE("/pet/{pet_id}")
    fun deletePet(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("pet_id") id: String? = App.preferences.Pet().id,
    ):Call<JsonObject>

    @POST("/user")
    fun postSignUpData(
        @Body user: User
    ): Call<JsonObject>

    //펫 백신 추가
    @FormUrlEncoded
    @POST("/pet/vaccination/{pet_id}")
    fun postVaccination(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("pet_id") id: String? = App.preferences.Pet().id,
        @Field("PET_VACCINATION_NAME") PET_VACCINATION_NAME: String,
        @Field("PET_VACCINATION_DATE") PET_VACCINATION_DATE: String,
        @Field("PET_VACCINATION_PERIOD") PET_VACCINATION_PERIOD: String
    ):Call<JsonObject>

    @FormUrlEncoded
    @POST("/pet/weight/{pet_id}")
    fun postWeight(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("pet_id") id: String? = App.preferences.Pet().id,
        @Field("PET_WEIGHT") PET_WEIGHT: String,
        @Field("PET_WEIGHT_DATE") PET_WEIGHT_DATE: String
    ): Call<JsonObject>

    //게시글 추가
    @FormUrlEncoded
    @Multipart
    @POST("/post")
    fun addPost(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Field("POST_CONTENT") POST_CONTENT: String,
        @Part image: MultipartBody.Part
    ):Call <JsonObject>


    //게시글 수정
    @Multipart
    @FormUrlEncoded
    @PUT("/post/{post_id}")
    fun updatePost(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("post_id") id: String,
        @Field("POST_TITLE") POST_TITLE: String,
        @Field("POST_CONTENT") POST_CONTENT: String,
        @Part image: MultipartBody.Part
    ):Call <JsonObject>

    //게시글 삭제
    @DELETE("/post/{post_id}")
    fun deletePost(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("post_id") id: String
    ):Call <JsonObject>

    //전체 게시글 확인하기
    @GET("/post/list")
    fun getAllPost(
    ):Call <CommunityResponse>

    //게시글하나 보기
    @GET("/post/{post_id}")
    fun getOnePost(
        @Path("post_id") id: String,
    ):Call <JsonObject>

    //내가 좋아요 한 글들 리스트 출력
    @GET("/post/getPostsByLike/{user_id}")
    fun getPostsByLike(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        //Todo-User_ID Path에 추가하기
//        @Path("user_id") id: String? = App.preferences.User().id,
    ):Call <JsonObject>

    //내가 쓴 글들 리스트 출력
    @GET("/post/getPostsByUserId/{user_id}")
    fun getPostsByUserId(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        //Todo-User_ID Path에 추가하기
//        @Path("user_id") id: String? = App.preferences.User().id,
    ):Call <JsonObject>

    //내가 댓글 단 글들 리스트 출력
    @GET("/post/getPostsByComment/{user_id}")
    fun getPostsByComment(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        //Todo-User_ID Path에 추가하기
//        @Path("user_id") id: String? = App.preferences.User().id,
    ):Call <JsonObject>
    //좋아요 추가
    @POST("/post/like/{post_id}")
    fun addLike(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("post_id") id: String,
    ):Call<JsonObject>

    //좋아요 취소
    @DELETE("/post/like/{post_id}")
    fun deleteLike(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("post_id") id: String,
    ):Call<JsonObject>

    //댓글 추가
    @FormUrlEncoded
    @POST("/post/comment/{post_id}")
    fun addComment(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Field("COMMENT_TEXT") COMMENT_TEXT:String,
        @Path("post_id") id: String,
    ):Call <JsonObject>

    //댓글 수정
    @FormUrlEncoded
    @PUT("/post/comment/{comment_id}")
    fun updateComment(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Field("COMMENT_TEXT") COMMENT_TEXT:String,
        @Path("comment_id") id: String,
    ):Call <JsonObject>

    //댓글 삭제
    @DELETE("/post/comment/{comment_id}")
    fun deleteComment(
        @Header("Authorization") token: String = "Bearer ${App.preferences.token}",
        @Path("comment_id") id: String,
    ):Call <JsonObject>
}