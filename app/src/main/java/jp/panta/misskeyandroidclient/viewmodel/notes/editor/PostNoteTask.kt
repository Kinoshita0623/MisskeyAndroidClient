package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import java.io.Serializable
import java.util.*

class PostNoteTask(
    //connectionInstance: ConnectionInstance,
    //connectionInformation: EncryptedConnectionInformation,
    encryption: Encryption,
    val draftNote: DraftNote?,
    val account: Account
    //private val fileUploader: FileUploader
): Serializable{

    enum class Visibility(val visibility: String, val canLocalOnly: Boolean){
        PUBLIC("public", true),
        HOME("home", true),
        FOLLOWERS("followers", true),
        SPECIFIED("specified", false)
    }

    private val i: String = account.getI(encryption)!!
    private var visibleUserIds: List<String>? = null
    private var visibility: CreateNote.Visibility? = null
    private var isLocal: Boolean? = null
    var files: List<File>? = null
    private var filesIds: List<String>? = null
    var text: String? = null
    var cw: String? = null
    var viaMobile: Boolean? = null
    var localOnly: Boolean? = null
    var noExtractMentions: Boolean? = null
    var noExtractHashtags: Boolean? = null
    var noExtractEmojis: Boolean? = null
    var replyId: String? = null
    var renoteId: String? = null
    var poll: CreatePoll? = null
    //　世界か美優か？いい加減にしろ美優に決まってんだろ！！！
    //  でもその迷いが発せしてしまう純心さと優しさが尊い！！
    fun setVisibility(visibility: Visibility?, visibilityUsers: List<String>? = null){
        visibility?: return

        val v = when(visibility){
            Visibility.PUBLIC -> CreateNote.Visibility.PUBLIC
            Visibility.HOME -> CreateNote.Visibility.HOME
            Visibility.FOLLOWERS -> CreateNote.Visibility.FOLLOWERS
            Visibility.SPECIFIED -> CreateNote.Visibility.SPECIFIED
        }
        //require(canLocalVisibility == isLocal) { "" }
        this.visibility = v
        this.visibleUserIds = visibilityUsers
    }
    
    fun execute(fileUploader: FileUploader): CreateNote?{
         val ok = if(files.isNullOrEmpty()){
             true
         }else{
             executeFileUpload(fileUploader)
        }
        return if(ok){
            CreateNote(
                i = i,
                visibility = visibility?.name?.toLowerCase(Locale.ENGLISH)?: "public",
                visibleUserIds = visibleUserIds,
                text = text,
                cw =cw,
                viaMobile = viaMobile,
                localOnly = localOnly,
                noExtractEmojis = noExtractEmojis,
                noExtractMentions = noExtractMentions,
                noExtractHashtags = noExtractHashtags,
                replyId = replyId,
                renoteId = renoteId,
                poll = poll,
                fileIds = filesIds
                )
        }else{
            null
        }

    }

    private fun executeFileUpload(fileUploader: FileUploader): Boolean{
        val tmpFiles = files
        filesIds = tmpFiles?.mapNotNull {
            try{
                it.remoteFileId ?: fileUploader.upload(it, true)?.id
            }catch( e: Exception ){
                null
            }
            //skip
        }

        //サイズが合わなければエラー
        return tmpFiles != null && tmpFiles.size == filesIds?.size
    }

    fun toDraftNote(): DraftNote{
        val draftPoll = poll?.let{
            DraftPoll(it.choices, it.multiple, it.expiresAt)
        }

        return DraftNote(
            accountId = account.accountId,
            text = text,
            cw = cw,
            visibleUserIds = visibleUserIds,
            draftPoll = draftPoll,
            visibility = visibility?.name?: "public",
            localOnly = isLocal,
            renoteId = renoteId,
            replyId = replyId
        ).apply{
            this.draftNoteId = draftNote?.draftNoteId
        }
    }

}