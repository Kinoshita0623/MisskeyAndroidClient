package jp.panta.misskeyandroidclient.model

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add antennaId TEXT")
    }
}

val MIGRATION_34_35 = object : Migration(34, 35){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add listId TEXT")
    }
}

val MIGRATION_35_36 = object : Migration(35, 36){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("create table 'reaction_user_setting'('reaction' TEXT not null, 'instance_domain' TEXT not null, 'weight' INTEGER not null, primary key('reaction', 'instance_domain'))")
    }
}

val MIGRATION_36_37 = object : Migration(36, 37){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("alter table setting add weight INTEGER")
    }
}

val MIGRATION_1_2 = object : Migration(1, 2){
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("create table 'draft_note'('draft_note_id' INTEGER primary key autoincrement,'accountId' TEXT not null, 'visibility' TEXT not null, 'text' TEXT, 'cw' TEXT, 'viaMobile' INTEGER, 'localOnly' INTEGER, 'noExtractMentions' INTEGER, 'noExtractHashtags' INTEGER, 'noExtractEmojis' INTEGER, 'replyId' TEXT, 'renoteId' TEXT, 'expiresAt' INTEGER, 'multiple' INTEGER, foreign key('accountId') references 'Account'('id') on update cascade on delete cascade)")
        database.execSQL("create table 'user_id'('userId' TEXT not null, 'draft_note_id' INTEGER not null, foreign key('draft_note_id') references'draft_note'('draft_note_id') on delete cascade on update cascade, primary key('userId', 'draft_note_id'))")
        database.execSQL("create table 'poll_choice'('choice' TEXT not null, 'weight' INTEGER not null, 'draft_note_id' INTEGER not null, foreign key('draft_note_id') references 'draft_note'('draft_note_id') on delete cascade on update cascade, primary key('choice', 'weight', 'draft_note_id'))")
        database.execSQL("create table 'draft_file'('remote_file_id' TEXT, 'file_path' TEXT, 'draft_note_id' INTEGER not null, 'file_id' INTEGER primary key autoincrement, foreign key('draft_note_id') references 'draft_note'('draft_note_id') on delete cascade on update cascade)")

        database.execSQL("create index 'index_draft_note_accountId_text' on 'draft_note'('accountId', 'text')")
        database.execSQL("create index 'index_user_id' on 'user_id'('draft_note_id')")
        database.execSQL("create index 'index_poll_choice_draft_note_id_choice' on 'poll_choice'('draft_note_id', 'choice')")
        database.execSQL("create index 'index_draft_file' on 'draft_file'('draft_note_id')")
    }
}