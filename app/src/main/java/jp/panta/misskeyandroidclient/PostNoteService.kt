package jp.panta.misskeyandroidclient

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.drive.OkHttpDriveFileUploader
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PostNoteService : IntentService("PostNoteService") {

    companion object{
        const val tag = "PostNoteService"
        const val EXTRA_NOTE_TASK = "jp.panta.misskeyandroidclient.EXTRA_NOTE_TASK"
    }

    override fun onHandleIntent(intent: Intent?) {
        val noteTask = intent?.getSerializableExtra(EXTRA_NOTE_TASK) as PostNoteTask?
        if(noteTask == null){
            Log.e(tag, "EXTRA_NOTE_TASKがnullです")
            return
        }
        val miApplication = applicationContext as MiApplication
        val account = miApplication.getCurrentAccount().value
        if(account == null){
            Log.e(tag, "Accountの取得に失敗しました")
            return
        }

        val uploader = OkHttpDriveFileUploader(applicationContext, account, GsonBuilder().create(), miApplication.getEncryption())
        val createNote = noteTask.execute(uploader)
        if(createNote == null){
            Log.d(tag, "ファイルのアップロードに失敗しました")
            saveDraftNote(noteTask.toDraftNote())
            return
        }

        Log.d(tag, "createNote: $createNote")
        val result =try{
             miApplication.getMisskeyAPI(account).create(createNote).execute()

        }catch(e: Exception){
            null
        }
        if(result?.code() in 200 until 300){
            Log.d(tag, "ノートの投稿に成功しました")
            removeExDraftNote(noteTask.draftNote)

        }else{
            Log.d(tag, "ノートの投稿に失敗しました")
            saveDraftNote(noteTask.toDraftNote())

        }

    }

    private fun removeExDraftNote(draftNote: DraftNote?){
        GlobalScope.launch(Dispatchers.IO){
            try{
                val app = applicationContext.applicationContext as MiApplication
                draftNote?.let{
                    app.draftNoteDao.deleteDraftNote(draftNote)
                }
            }catch(e: Exception){
                Log.e("PostNoteTask", "delete ex draft note error", e)
            }
        }
    }

    private fun saveDraftNote(draftNote: DraftNote){
        GlobalScope.launch(Dispatchers.IO) {
            try{
                val app = applicationContext as MiApplication
                app.draftNoteDao.fullInsert(draftNote)
            }catch(e: Exception){
                Log.e("PostNoteTask", "save draft note error", e)
            }
        }
    }


}
