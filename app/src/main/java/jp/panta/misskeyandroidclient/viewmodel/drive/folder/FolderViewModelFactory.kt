package jp.panta.misskeyandroidclient.viewmodel.drive.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.lang.IllegalArgumentException

class FolderViewModelFactory(private val connectionInstance: ConnectionInstance, private val miApplication: MiApplication, private val folderId: String?) : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass == FolderViewModel::class.java){
            val api = miApplication.misskeyAPIService!!
            return FolderViewModel(connectionInstance, api, folderId) as T
        }
        throw IllegalArgumentException("FolderViewModel::class.javaを指定してください")
    }
}