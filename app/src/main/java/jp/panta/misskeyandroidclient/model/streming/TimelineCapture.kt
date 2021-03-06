package jp.panta.misskeyandroidclient.model.streming

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.Expose
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.settings.SettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.HasReplyToNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.util.*
import kotlin.collections.HashMap

class TimelineCapture(
    override val account: Account,
    val settingStore: SettingStore
) : AbsObserver(){

    interface Observer{
        fun onReceived(note: PlaneNoteViewData)
    }
    class TimelineObserver(
        override val type: String = "connect",
        val body: RequestBody,
        @Expose
        val observer : Observer?
    ) : StreamingAction{
        companion object{
            fun create(channel: Pageable, observer: Observer): TimelineObserver?{
                val timelineType = when(channel){
                    is Pageable.HomeTimeline -> "homeTimeline"
                    is Pageable.HybridTimeline -> "hybridTimeline"
                    is Pageable.LocalTimeline -> "localTimeline"
                    is Pageable.GlobalTimeline -> "globalTimeline"
                    else -> null
                }
                timelineType?: return null
                return TimelineObserver(body = RequestBody(id = UUID.randomUUID().toString(), channel = timelineType), observer = observer)
            }

            fun createDisconnect(id: String): TimelineObserver?{
                return TimelineObserver(type = "disconnect",
                    body = RequestBody(id = id, channel = null), observer = null)
            }
        }

        /*fun updateId(){
            body.id = UUID.randomUUID().toString()
        }*/
    }

    data class RequestBody(
        val id: String,
        val channel: String?
    )

    private data class Response(override val type: String, val body: Body): StreamingAction
    private data class Body(val id: String, val type: String, val body: Note?)
    /*
    main
    {
      type: "channel",
      body: { id: "72943",
              type: "readAllNotifications,
              body: null
             }
     }

     */

    /*
    timeline
    {
      type: "channel",
        body: {
          id: "23948203",
          type: "note",
          body: Note
     }
     */

    override var streamingAdapter: StreamingAdapter? = null

    private val observerMap = HashMap<String, TimelineObserver>()

    private val gson = Gson()

    override fun onConnect() {
        val observers = synchronized(observerMap){
            observerMap.values
        }
        observers.forEach {
            streamingAdapter?.send(gson.toJson(it))
        }

    }

    override fun onClosing() {
        synchronized(observerMap){
            observerMap.values
        }.forEach(::removeChannelObserver)

    }

    // リソース解放は接続によって管理されるのではなく、オブジェクトが再利用される以上利用するクラスによって管理されるべきではないか？
    override fun onDisconnect() = Unit

    override fun onReceived(msg: String) {
        try{
            val res = gson.fromJson(msg, Response::class.java)
            //Log.d("TimelineCapture", "onReceived: $msg")
            val note = res.body.body
            val id = res.body.id
            if(note != null){
                val viewData = if(note.reply == null){
                    PlaneNoteViewData(note, account, DetermineTextLengthSettingStore(settingStore))
                }else{
                    HasReplyToNoteViewData(note, account, DetermineTextLengthSettingStore(settingStore))
                }
                val observer = synchronized(observerMap){
                    observerMap[id]
                }
                observer?.observer?.onReceived(viewData)
            }
        }catch(e: JsonSyntaxException){
            //Log.d("TimelineCapture", "遺物排除")
        }catch(e: NullPointerException){
            //Log.d("TimelineCapture", "遺物排除")
        }

    }

    fun addChannelObserver(observer: TimelineObserver){
        synchronized(observerMap){
            observerMap[observer.body.id] = observer
        }
        Log.d("TimelineCapture", "登録しました: ${gson.toJson(observer)}, streamingAdapter is active:${streamingAdapter != null}")
        streamingAdapter?.send(gson.toJson(observer))
    }

    fun removeChannelObserver(observer: TimelineObserver){
        val removed = synchronized(observerMap){
            observerMap.remove(observer.body.id)
        }
        if(removed != null){
            streamingAdapter?.send(gson.toJson(TimelineObserver.createDisconnect(removed.body.id)))
        }
    }
}