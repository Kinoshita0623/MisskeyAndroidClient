package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.poll.PollEditor
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.NullPointerException
import java.util.*
import kotlin.collections.ArrayList

class NoteEditorViewModel(
    //private val accountRelation: AccountRelation,
    //private val misskeyAPI: MisskeyAPI,
    private val miCore: MiCore,
    private val draftNoteDao: DraftNoteDao,
    //meta: Meta,
    private val replyToNoteId: String? = null,
    private val quoteToNoteId: String? = null,
    private val encryption: Encryption = miCore.getEncryption(),
    val note: Note? = null,
    val draftNote: DraftNote? = null
) : ViewModel(){


    val currentAccount = miCore.getCurrentAccount()

    val currentUser = Transformations.map(currentAccount){ account ->
        UserViewData(account.remoteId).apply{

            val i = account.getI(miCore.getEncryption())
            i?.let{
                setApi(i, miCore.getMisskeyAPI(account))
            }
        }
    }

    val hasCw = MutableLiveData<Boolean>(!note?.cw.isNullOrBlank()|| !draftNote?.cw.isNullOrBlank())
    val cw = MutableLiveData<String>(note?.cw?: draftNote?.cw)
    val text = MutableLiveData<String>(note?.text?: draftNote?.text)
    var maxTextLength = Transformations.map(currentAccount){
        miCore.getCurrentInstanceMeta()?.maxNoteTextLength?: 1500
    }
    /*val textRemaining = Transformations.map(text){ t: String? ->
        (maxTextLength.value?: 1500) - (t?.codePointCount(0, t.length)?: 0)
    }*/
    val textRemaining = MediatorLiveData<Int>().apply{
        addSourceChain(maxTextLength){ maxSize ->
            val t = text.value
            value = (maxSize?: 1500) - (t?.codePointCount(0, t.length)?: 0)
        }
        addSource(text){ t ->
            val max = maxTextLength.value?: 1500
            value = max - (t?.codePointCount(0, t.length)?: 0)
        }
    }


    val files = MediatorLiveData<List<File>>().apply{
        this.postValue(
            note?.files?.map{
                it.toFile(getInstanceBaseUrl()?: "")
            }?:draftNote?.files?: emptyList<File>()
        )
    }

    val totalImageCount = MediatorLiveData<Int>().apply{

        this.addSource(files){
            Log.d("NoteEditorViewModel", "list$it, sizeは: ${it.size}")
            this.value = it.size
        }
    }


    val isPostAvailable = MediatorLiveData<Boolean>().apply{
        this.addSource(textRemaining){
            val totalImageTmp = totalImageCount.value
            this.value =  it in 0 until (maxTextLength.value?: 1500)
                    || (totalImageTmp != null && totalImageTmp > 0 && totalImageTmp <= 4)
                    || quoteToNoteId != null
        }
        this.addSource(totalImageCount){
            val tmpTextSize = textRemaining.value
            this.value = tmpTextSize in 0 until (maxTextLength.value?: 1500)
                    || (it != null && it > 0 && it <= 4)
                    || quoteToNoteId != null
        }
    }

    private val mVisibilityEnums = PostNoteTask.Visibility.values()
    private val mExVisibility = mVisibilityEnums.firstOrNull {
        it.visibility == note?.visibility?.toLowerCase(Locale.US)
                || it.visibility == draftNote?.visibility?.toLowerCase(Locale.US)
    }
    val visibility = MutableLiveData<PostNoteTask.Visibility>(mExVisibility?: PostNoteTask.Visibility.PUBLIC)

    val isLocalOnly = MediatorLiveData<Boolean>().apply{
        addSource(visibility){
            value = it.canLocalOnly && value == true
        }
    }

    val isLocalOnlyEnabled = MediatorLiveData<Boolean>().apply{
        addSource(visibility){
            value = it?.canLocalOnly == true
        }
    }

    val showVisibilitySelectionEvent = EventBus<Unit>()
    val visibilitySelectedEvent = EventBus<Unit>()

    val address = MutableLiveData<List<UserViewData>>(
        note?.visibleUserIds?.map(::setUpUserViewData)
            ?: draftNote?.visibleUserIds?.map(::setUpUserViewData)
    )

    private fun setUpUserViewData(userId: String) : UserViewData{
        return UserViewData(userId).apply{
            val ci = getCurrentInformation()
            val i = ci?.getI(miCore.getEncryption())
            i?.let{
                setApi(i, miCore.getMisskeyAPI(ci))
            }
        }
    }

    val isSpecified = Transformations.map(visibility){
        it == PostNoteTask.Visibility.SPECIFIED
    }

    val poll = MutableLiveData<PollEditor?>(
        note?.poll?.let{
            PollEditor(it)
        }?: draftNote?.draftPoll?.let{
            PollEditor(it)
        }
    )

    val noteTask = MutableLiveData<PostNoteTask>()

    val showPollDatePicker = EventBus<Unit>()
    val showPollTimePicker = EventBus<Unit>()



    val isSaveNoteAsDraft = EventBus<Long?>()
    init{
        currentAccount.observeForever {
            miCore.getCurrentInstanceMeta()
        }
    }

    fun post(){
        currentAccount.value?.let{ account ->
            val noteTask = PostNoteTask(encryption, draftNote, account)
            noteTask.cw = if(cw.value.isNullOrBlank()) null else cw.value
            noteTask.files = files.value
            noteTask.text =text.value
            noteTask.poll = poll.value?.buildCreatePoll()
            noteTask.renoteId = quoteToNoteId
            noteTask.replyId = replyToNoteId
            noteTask.setVisibility(visibility.value, address.value?.map{
                it.userId
            })
            noteTask.localOnly = this.isLocalOnly.value?: false
            this.noteTask.postValue(noteTask)
        }

    }

    fun add(file: File){
        val files = files.value.toArrayList()
        files.add(
            file
        )
        this.files.value = files
    }

    fun add(fp: FileProperty){
        add(fp.toFile(getInstanceBaseUrl()?: ""))
    }



    fun addAllFileProperty(fpList: List<FileProperty>){
        val files = files.value.toArrayList()
        files.addAll(fpList.map{
            it.toFile(getInstanceBaseUrl()?: "")
        })
        this.files.value = files
    }

    fun removeFileNoteEditorData(file: File){
        val files = files.value.toArrayList()
        files.remove(file)
        this.files.value = files
    }

    fun localFileTotal(): Int{
        return files.value?.filter{
            it.remoteFileId == null
        }?.size?: 0
    }

    fun driveFileTotal(): Int{
        return files.value?.filter{
            it.remoteFileId != null
        }?.size?: 0
    }

    fun fileTotal(): Int{
        return files.value?.size?: 0
    }

    fun remoteFiles(): List<File>{
        return files.value?.filter{
            it.remoteFileId != null
        }?: emptyList()
    }



    fun changeCwEnabled(){
        hasCw.value = !(hasCw.value?: false)
        if(hasCw.value == false){
            cw.value = ""
        }
    }

    fun enablePoll(){
        val p = poll.value
        if(p == null){
            poll.value = PollEditor()
        }
    }

    fun disablePoll(){
        val p = poll.value
        if(p != null){
            poll.value = null
        }
    }

    fun showVisibilitySelection(){
        showVisibilitySelectionEvent.event = Unit
    }

    fun setVisibility(visibility: PostNoteTask.Visibility){
        this.visibility.value = visibility
        this.visibilitySelectedEvent.event = Unit
    }



    private fun List<File>?.toArrayList(): ArrayList<File>{
        return if(this == null){
            ArrayList<File>()
        }else{
            ArrayList<File>(this)
        }
    }

    fun setAddress(added: Array<String>, removed: Array<String>){
        val list = address.value?.let{
            ArrayList(it)
        }?: ArrayList()

        list.addAll(
            added.map{
                UserViewData(it).apply{
                    val ci = getCurrentInformation()
                    val i = ci?.getI(miCore.getEncryption())
                    i?.let{
                        setApi(i, miCore.getMisskeyAPI(ci))
                    }
                }
            }
        )

        list.removeAll { uv ->
            removed.any{
                uv.userId == it
            }
        }
        address.postValue(list)
    }


    fun addMentionUsers(users: List<User>, pos: Int): Int{
        val mentionBuilder = StringBuilder()
        users.forEachIndexed { index, it ->
            val userName = it.getDisplayUserName()
            if(index < users.size - 1){
                mentionBuilder.appendln(userName)
            }else{
                mentionBuilder.append(userName)
            }
        }
        val builder = StringBuilder(text.value?: "")
        builder.insert(pos, mentionBuilder.toString())
        text.value = builder.toString()
        return pos + mentionBuilder.length
    }

    fun addEmoji(emoji: Emoji, pos: Int): Int{
        return addEmoji(":${emoji.name}:", pos)
    }

    fun addEmoji(emoji: String, pos: Int): Int{
        val builder = StringBuilder(text.value?: "")
        builder.insert(pos, emoji)
        text.value = builder.toString()
        Log.d("NoteEditorViewModel", "position:${pos + emoji.length - 1}")
        return pos + emoji.length
    }


    fun toDraftNote(): DraftNote{
        return DraftNote(
            accountId = currentAccount.value?.accountId!!,
            text = text.value,
            cw = cw.value,
            visibleUserIds = address.value?.map{
                it.userId
            },
            draftPoll = poll.value?.toDraftPoll(),
            visibility = visibility.value?.visibility?: "public",
            localOnly = visibility.value?.canLocalOnly,
            renoteId = quoteToNoteId,
            replyId = replyToNoteId,
            files = files.value
        ).apply{
            this.draftNoteId = draftNote?.draftNoteId
        }
    }
    fun saveDraft(){
        if(!canSaveDraft()){
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try{
                val dfNote = toDraftNote()

                try{
                    isSaveNoteAsDraft.event = draftNoteDao.fullInsert(dfNote)
                }catch(e: Exception){
                    Log.e("NoteEditorVM", "下書き書き込み中にエラー発生：失敗してしまった", e)
                }
            }catch(e: IOException){

            }catch(e: NullPointerException){
                Log.e("NoteEditorVM", "下書き保存に失敗した", e)

            }catch (e: Throwable){
                Log.e("NoteEditorVM", "下書き保存に失敗した", e)

            }

        }
    }

    fun canSaveDraft(): Boolean{
        return !(cw.value.isNullOrBlank()
                && text.value.isNullOrBlank()
                && files.value.isNullOrEmpty()
                && poll.value?.choices?.value.isNullOrEmpty()
                && address.value.isNullOrEmpty())
    }

    private fun <T, S>MediatorLiveData<T>.addSourceChain(liveData: LiveData<S>, observer: (out: S)-> Unit): MediatorLiveData<T>{
        this.addSource(liveData, observer)
        return this
    }

    private fun getInstanceBaseUrl(): String?{
        return currentAccount.value?.instanceDomain
    }

    fun clear(){
        text.value = ""
        cw.value = ""
        files.value = emptyList()
        address.value = emptyList()
        poll.value = null
    }

    private fun getCurrentInformation(): Account?{
        return miCore.getCurrentAccount().value
    }

}