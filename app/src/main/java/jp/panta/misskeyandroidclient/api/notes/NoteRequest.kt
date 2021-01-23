package jp.panta.misskeyandroidclient.api.notes

import com.google.gson.annotations.SerializedName
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import java.io.Serializable

data class NoteRequest(
    @SerializedName("i") val i: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("withFiles") val withFiles: Boolean? = null,
    @SerializedName("fileType") val fileType: String? = null,
    @SerializedName("excludeNsfw") val excludeNsfw: Boolean? = null,
    @SerializedName("limit") val limit: Int? = 20,
    @SerializedName("sinceId") val sinceId: String? = null,
    @SerializedName("untilId") val untilId: String? = null,
    @SerializedName("sinceDate") val sinceDate: Long? = null,
    @SerializedName("untilDate") val untilDate: Long? = null,
    @SerializedName("query") val query: String? = null,
    @SerializedName("tag") val tag: String? = null,
    @SerializedName("includeLocalRenotes") val includeLocalRenotes: Boolean? = null,
    @SerializedName("includeMyRenotes") val includeMyRenotes: Boolean? = null,
    @SerializedName("includeRenotedMyNotes") val includeRenotedMyNotes: Boolean? = null,
    @SerializedName("noteId") val noteId: String? = null,
    @SerializedName("antennaId") val antennaId: String? = null,
    @SerializedName("listId") val listId: String? = null,
    val following: Boolean? = null,
    val visibility: String? = null,
    val reply: Boolean? = null,
    val renote: Boolean? = null,
    val poll: Boolean? = null,
    val offset: Int? = null,
    val includeReplies: Boolean? = null,
    val host: String? = null,
    val markAsRead: Boolean? = null
): Serializable{


    class Builder(
        val pageable: Pageable,
        var i: String?,
        var includes: Include? = null,
        var limit: Int = 20
    ){

        fun build(conditions: Conditions?): NoteRequest {
            val params = pageable.toParams()
            return NoteRequest(
                i = i,
                userId = params.userId,
                withFiles = params.withFiles,
                excludeNsfw = params.excludeNsfw,
                limit = limit,
                sinceId = conditions?.sinceId,
                untilId = conditions?.untilId,
                untilDate = conditions?.untilDate,
                sinceDate = conditions?.sinceDate,
                query = params.query,
                tag = params.tag,
                includeRenotedMyNotes = includes?.includeRenotedMyNotes?: params.includeRenotedMyRenotes,
                includeMyRenotes = includes?.includeMyRenotes?: params.includeMyRenotes,
                includeReplies = params.includeReplies,
                includeLocalRenotes = includes?.includeLocalRenotes?: params.includeLocalRenotes,
                following = params.following,
                poll = params.poll,
                offset = params.offset,
                visibility = params.visibility,
                host = params.host,
                antennaId = params.antennaId,
                markAsRead = params.markAsRead,
                renote = params.renote,
                reply = params.reply,
                listId = params.listId,
                noteId = params.noteId
            )
        }

    }
    /*class Builder(
        val pageableTimeline: Page.Timeline,
        var include: Include? = null
    ){


        fun build(i: String, conditions: Conditions?): NoteRequest{
            return when(pageableTimeline){
                is Page.HomeTimeline -> {
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.HybridTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.GlobalTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.LocalTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles
                    )
                }
                is Page.UserListTimeline ->{
                    NoteRequest( i = i,
                        listId = pageableTimeline.listId,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeLocalRenotes = pageableTimeline.includeLocalRenotes?: include?.includeLocalRenotes,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        includeRenotedMyNotes = pageableTimeline.includeRenotedMyRenotes?: include?.includeRenotedMyNotes,
                        withFiles = pageableTimeline.withFiles
                    )
                }

                is Page.Mention ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        following = pageableTimeline.following,
                        visibility = pageableTimeline.visibility
                    )
                }
                is Page.SearchByTag ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        withFiles = pageableTimeline.withFiles,
                        tag = pageableTimeline.tag

                    )
                }
                is Page.UserTimeline ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        includeMyRenotes = pageableTimeline.includeMyRenotes?: include?.includeMyRenotes,
                        withFiles = pageableTimeline.withFiles,
                        includeReplies = pageableTimeline.includeReplies,
                        userId = pageableTimeline.userId
                    )
                }
                is Page.Search ->{
                    NoteRequest( i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        userId = pageableTimeline.userId,
                        query = pageableTimeline.query,
                        host = pageableTimeline.host
                    )
                }
                is Page.Favorite ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate
                    )
                }
                is Page.Featured ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate
                    )
                }

                is Page.Antenna ->{
                    NoteRequest(
                        i = i,
                        sinceId = conditions?.sinceId,
                        untilId = conditions?.untilId,
                        sinceDate = conditions?.sinceDate,
                        untilDate = conditions?.untilDate,
                        antennaId = pageableTimeline.antennaId
                    )
                }
                else -> throw IllegalArgumentException("type: ${pageableTimeline.javaClass}")

            }
        }
    }*/



    data class Conditions(
        @SerializedName("sinceId") val sinceId: String? = null,
        @SerializedName("untilId") val untilId: String? = null,
        @SerializedName("sinceDate") val sinceDate: Long? = null,
        @SerializedName("untilDate") val untilDate: Long? = null
    )

    data class Include(
        val includeLocalRenotes: Boolean? = null,
        val includeMyRenotes: Boolean? = null,
        val includeRenotedMyNotes: Boolean? = null
    )
    fun makeSinceId(id: String): NoteRequest {
        return this.copy(sinceId = id, untilId = null, untilDate = null, sinceDate = null)
    }
    fun makeUntilId(id: String): NoteRequest {
        return this.copy(sinceId = null, untilId = id, untilDate = null, sinceDate = null)
    }



}