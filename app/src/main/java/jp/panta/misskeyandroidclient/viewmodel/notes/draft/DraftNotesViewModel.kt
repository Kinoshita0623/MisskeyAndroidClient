package jp.panta.misskeyandroidclient.viewmodel.notes.draft

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftNotesViewModel(
    val draftNoteDao: DraftNoteDao,
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miApplication: MiApplication
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DraftNotesViewModel(miApplication.draftNoteDao, miApplication) as T

        }
    }

    val draftNotes = object : MediatorLiveData<List<DraftNoteViewData>>(){
        override fun onActive() {
            super.onActive()
            if(value.isNullOrEmpty()){
                loadDraftNotes()
            }
        }
    }.apply{
        addSource(miCore.getCurrentAccount()){
            loadDraftNotes()
        }
    }

    val isLoading = MutableLiveData<Boolean>()

    fun loadDraftNotes(ac: Account){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val notes = draftNoteDao.findDraftNotesByAccount(ac.accountId)
                draftNotes.postValue(notes.map{
                    DraftNoteViewData(it)
                }.asReversed())
            }catch(e: Exception){

            }finally {
                isLoading.postValue(false)
            }
        }
    }

    fun loadDraftNotes(){
        miCore.getCurrentAccount().value?.let{
            loadDraftNotes(it)
        }
    }

    fun detachFile(file: File?) {
        file?.localFileId?.let{
            val notes = ArrayList(draftNotes.value?: emptyList())
            val targetNote = (notes.firstOrNull { dNote ->
                dNote.note.value?.files?.any {
                    it.localFileId == file.localFileId
                }?: false
            }?: return).note.value ?: return

            val updatedFiles = ArrayList(targetNote.files?: emptyList())
            updatedFiles.remove(file)

            viewModelScope.launch(Dispatchers.IO){
                try{
                    draftNoteDao.deleteFile(targetNote.draftNoteId!!, file.localFileId)

                    loadDraftNotes()
                }catch(e: Exception){
                    Log.e("DraftNotesViewModel", "更新に失敗した", e)
                }
            }
        }
    }

    fun deleteDraftNote(draftNote: DraftNote){
        viewModelScope.launch(Dispatchers.IO){
            try{
                draftNoteDao.deleteDraftNote(draftNote)
                loadDraftNotes()
            }catch(e: Exception){
                Log.e("DraftNotesViewModel", "下書きノート削除に失敗しました", e)
            }
        }
    }




}