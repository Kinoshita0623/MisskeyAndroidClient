package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.db.InMemoryNoteEventStore
import java.util.concurrent.ConcurrentHashMap

class NoteEventStoreFactory {

    private val accountAndStore = ConcurrentHashMap<Long, NoteEventStore>()

    fun create(account: Account) : NoteEventStore{
        synchronized(this){
            var store = accountAndStore[account.accountId]
            if(store == null){
                store = InMemoryNoteEventStore(account)
                accountAndStore[account.accountId] = store
            }
            return store
        }
    }
}