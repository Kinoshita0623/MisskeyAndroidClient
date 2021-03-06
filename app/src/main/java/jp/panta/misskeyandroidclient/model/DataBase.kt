package jp.panta.misskeyandroidclient.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.core.*
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNoteDao
import jp.panta.misskeyandroidclient.model.notes.draft.db.DraftFileDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.DraftNoteDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.PollChoiceDTO
import jp.panta.misskeyandroidclient.model.notes.draft.db.UserIdDTO
import jp.panta.misskeyandroidclient.model.notes.reaction.*
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.db.AccountDAO
import jp.panta.misskeyandroidclient.model.account.page.TimelinePageTypeConverter
import jp.panta.misskeyandroidclient.model.account.page.db.PageDAO
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.db.EmojiDTO
import jp.panta.misskeyandroidclient.model.instance.db.MetaDAO
import jp.panta.misskeyandroidclient.model.instance.db.MetaDTO

@Database(
    entities = [
        EncryptedConnectionInformation::class,
        ReactionHistory::class,
        jp.panta.misskeyandroidclient.model.core.Account::class,
        ReactionUserSetting::class,
        jp.panta.misskeyandroidclient.model.Page::class,
        PollChoiceDTO::class,
        UserIdDTO::class,
        DraftFileDTO::class,
        DraftNoteDTO::class,

        UrlPreview::class,
        Account::class,
        Page::class,
        MetaDTO::class,
        EmojiDTO::class

    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(PageTypeConverter::class, DateConverter::class, TimelinePageTypeConverter::class)
abstract class DataBase : RoomDatabase(){
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
    abstract fun connectionInformationDao(): ConnectionInformationDao
    abstract fun accountDao(): AccountDao
    abstract fun reactionHistoryDao(): ReactionHistoryDao
    abstract fun reactionUserSettingDao(): ReactionUserSettingDao
    abstract fun pageDao(): PageDao
    abstract fun draftNoteDao(): DraftNoteDao

    abstract fun urlPreviewDAO(): UrlPreviewDAO

    abstract fun accountDAO(): AccountDAO
    abstract fun pageDAO(): PageDAO

    abstract fun metaDAO(): MetaDAO
    //abstract fun connectionInstanceDao(): ConnectionInstanceDao
}