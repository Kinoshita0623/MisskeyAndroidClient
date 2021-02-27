package jp.panta.misskeyandroidclient.streaming.channel

import com.google.gson.Gson
import jp.panta.misskeyandroidclient.streaming.*
import jp.panta.misskeyandroidclient.streaming.network.StreamingEventListener
import jp.panta.misskeyandroidclient.streaming.network.Socket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ChannelAPI(
    val socket: Socket,
) : Reconnectable, StreamingEventListener {

    enum class Type {
        MAIN, HOME, LOCAL, HYBRID, GLOBAL
    }


    private val listenersMap = ConcurrentHashMap<Type, HashMap<String,(ChannelBody)->Unit>>(
        mapOf(
            Type.MAIN to hashMapOf(),
            Type.HOME to hashMapOf(),
            Type.LOCAL to hashMapOf(),
            Type.HYBRID to hashMapOf(),
            Type.GLOBAL to hashMapOf()
        )
    )

    private val typeIdMap = ConcurrentHashMap<Type, String>()

    @ExperimentalCoroutinesApi
    fun connect(type: Type) : Flow<ChannelBody> {
        return channelFlow {
            val listenId = UUID.randomUUID().toString()
            connect(type, listenId){
                offer(it)
            }

            awaitClose {
                disconnect(type, listenId)
            }
        }
    }

    private fun connect(type: Type, listenId: String, listener: (ChannelBody)->Unit) {
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) == true){
                return@synchronized
            }

            listenersMap[type]?.let{ map ->
                map[listenId] = listener
            }
            if(typeIdMap[type] == null){
                sendConnect(type)
            }

        }
    }



    override fun handle(e: StreamingEvent): Boolean {
        if(e is ChannelEvent) {
            synchronized(listenersMap) {

                listenersMap.values.forEach { listeners ->
                    listeners.filter {
                        it.key == e.body.id
                    }.values.forEach { callback ->
                        callback.invoke(e.body)
                    }
                }
            }
            return true
        }

        return false
    }

    /**
     * 接続メッセージを現在の状態にかかわらずサーバーに送信する
     */
    private fun sendConnect(type: Type): Boolean {
        val body = when(type){
            Type.GLOBAL -> Send.Connect.Type.GLOBAL_TIMELINE
            Type.HYBRID -> Send.Connect.Type.HYBRID_TIMELINE
            Type.LOCAL -> Send.Connect.Type.LOCAL_TIMELINE
            Type.HOME -> Send.Connect.Type.HOME_TIMELINE
            Type.MAIN -> Send.Connect.Type.MAIN
        }

        val id = UUID.randomUUID().toString()
        if(
            socket.send(Send.Connect(Send.Connect.Body(channel = body, id = id)).toJson())
        ){
            typeIdMap[type] = id
            return true
        }
        return false
    }

    private fun disconnect(type: Type, listenId: String) {
        synchronized(listenersMap) {
            if(listenersMap[type]?.contains(listenId) != true){
                return@synchronized
            }

            listenersMap[type]?.remove(listenId)

            // 誰にも使われていなければサーバーからChannelへの接続を開放する
            trySendDisconnect(type)

        }
    }

    private fun trySendDisconnect(type: Type) {
        if(listenersMap[type].isNullOrEmpty()){
            val id = typeIdMap.remove(type)
            if(id != null){
                socket.send((Send.Disconnect(Send.Disconnect.Body(id)).toJson()))
            }
        }
    }

    /**
     * 再接続処理
     */
    override fun onReconnect() {
        synchronized(listenersMap) {
            val types = typeIdMap.keys
            typeIdMap.clear()
            types.forEach {
                sendConnect(it)
            }
        }
    }

}