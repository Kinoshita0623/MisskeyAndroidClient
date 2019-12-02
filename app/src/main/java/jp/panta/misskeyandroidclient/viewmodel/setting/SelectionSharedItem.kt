package jp.panta.misskeyandroidclient.viewmodel.setting

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.StringRes
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

class SelectionSharedItem (
    override val key: String,
    override val titleStringRes: Int,
    val default: Int,
    val choices: List<Choice>,
    private val context: Context
): SharedItem(){
    class Choice(@StringRes val stringRes: Int, val id: Int, context: Context){
        val title = context.getString(stringRes)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Choice

            if (stringRes != other.stringRes) return false
            if (id != other.id) return false
            if (title != other.title) return false

            return true
        }

        override fun hashCode(): Int {
            var result = stringRes
            result = 31 * result + id
            result = 31 * result + (title.hashCode())
            return result
        }
    }
    val title: String = context.getString(titleStringRes)

    val choice = IntSharedPreferenceLiveData(
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        default = default,
        key = key
    )

    /*val choosing: String
        get() {

        }*/
    /**
     * 選択中のアイテムのタイトルがここに入る
     */
    val choosing = Transformations.map(choice){
        val stringRes = choices.first {choice ->
            choice.id == it

        }.stringRes
        context.getString(stringRes)
    }


    fun setChoice(choice: Choice){
        this.choice.value = choice.id
    }


    val isSelecting = MutableLiveData<Boolean>(false)

    fun changeSelectingState(){
        val now = isSelecting.value?: false
        isSelecting.value = !now
    }



}