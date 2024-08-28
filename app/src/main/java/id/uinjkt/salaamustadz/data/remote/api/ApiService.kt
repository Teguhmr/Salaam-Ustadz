package id.uinjkt.salaamustadz.data.remote.api

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("send")
    fun sendMessage(
        @Field("token") token: String,
        @Field("user_id") user_id: String,
        @Field("chat_room_id") chat_room_id: String,
        @Field("name") name: String,
        @Field("notification_id") notification_id: String,
        @Field("message") message: String,
        @Field("fcm_token") fcm_token: String,
        @Field("type_message") type_message: String
    ): Call<String>
}