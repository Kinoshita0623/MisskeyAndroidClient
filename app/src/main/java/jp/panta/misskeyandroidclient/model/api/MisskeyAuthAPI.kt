package jp.panta.misskeyandroidclient.model.api

import jp.panta.misskeyandroidclient.model.auth.AccessToken
import jp.panta.misskeyandroidclient.model.auth.AppSecret
import jp.panta.misskeyandroidclient.model.auth.Session
import jp.panta.misskeyandroidclient.model.auth.UserKey
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MisskeyAuthAPI {
    //auth
    @POST("api/auth/session/generate")
    fun generateSession(@Body appSecret: AppSecret): Call<Session>

    @POST("api/auth/session/userkey")
    fun getAccessToken(@Body userKey: UserKey): Call<AccessToken>
}