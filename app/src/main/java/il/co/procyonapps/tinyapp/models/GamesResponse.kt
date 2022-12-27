package il.co.procyonapps.tinyapp.models


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


class GamesResponse : ArrayList<GamesResponse.GamesResponseItem>(){
    @JsonClass(generateAdapter = true)
    data class GamesResponseItem(
        @Json(name = "author")
        val author: String,
        @Json(name = "image")
        val image: String,
        @Json(name = "title")
        val title: String
    )
}