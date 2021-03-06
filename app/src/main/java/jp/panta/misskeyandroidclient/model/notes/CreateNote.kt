package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import java.lang.IllegalArgumentException

data class CreateNote(
    val i: String,
    val visibility: String = "public",
    val visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    var fileIds: List<String>? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val poll: CreatePoll? = null


){
    data class Response(val createdNote: Note)

    enum class Visibility(val isLocalOnlyPossible: Boolean){
        PUBLIC(true),
        HOME(true),
        FOLLOWERS(true),
        SPECIFIED(false)

    }
}