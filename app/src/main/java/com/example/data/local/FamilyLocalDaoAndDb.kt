package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyLocalDao {

    @Query("SELECT * FROM local_user_profiles WHERE userId = :userId LIMIT 1")
    fun observeUserProfile(userId: String): Flow<LocalUserProfileEntity?>

    @Query("SELECT * FROM local_user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): LocalUserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(entity: LocalUserProfileEntity)

    @Query("SELECT * FROM local_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): LocalAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(entity: LocalAccountEntity)

    @Query("DELETE FROM local_accounts WHERE email = :email")
    suspend fun deleteAccountByEmail(email: String)

    @Query("SELECT * FROM local_families WHERE id = :familyId LIMIT 1")
    fun observeFamily(familyId: String): Flow<LocalFamilyEntity?>

    @Query("SELECT * FROM local_families WHERE id = :familyId LIMIT 1")
    suspend fun getFamily(familyId: String): LocalFamilyEntity?

    @Query("SELECT * FROM local_families WHERE inviteCode = :inviteCode LIMIT 1")
    suspend fun getFamilyByInviteCode(inviteCode: String): LocalFamilyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFamily(entity: LocalFamilyEntity)

    @Query("SELECT * FROM local_members WHERE familyId = :familyId ORDER BY createdAtMs ASC")
    fun observeMembers(familyId: String): Flow<List<LocalMemberEntity>>

    @Query("SELECT * FROM local_members WHERE familyId = :familyId AND userId = :userId LIMIT 1")
    suspend fun getMember(familyId: String, userId: String): LocalMemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMember(entity: LocalMemberEntity)

    @Query("DELETE FROM local_members WHERE familyId = :familyId AND userId = :userId")
    suspend fun deleteMember(familyId: String, userId: String)

    @Query("SELECT * FROM local_groups WHERE familyId = :familyId ORDER BY updatedAtMs DESC")
    fun observeGroups(familyId: String): Flow<List<LocalGroupEntity>>

    @Query("SELECT * FROM local_groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroup(groupId: String): LocalGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGroup(entity: LocalGroupEntity)

    @Query("DELETE FROM local_groups WHERE familyId = :familyId AND id = :groupId")
    suspend fun deleteGroup(familyId: String, groupId: String)

    @Query("SELECT * FROM local_private_chats WHERE familyId = :familyId ORDER BY updatedAtMs DESC")
    fun observePrivateChats(familyId: String): Flow<List<LocalPrivateChatEntity>>

    @Query("SELECT * FROM local_private_chats WHERE id = :chatId LIMIT 1")
    suspend fun getPrivateChat(chatId: String): LocalPrivateChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrivateChat(entity: LocalPrivateChatEntity)

    @Query("SELECT * FROM local_messages WHERE familyId = :familyId AND roomId = :roomId ORDER BY createdAtMs ASC")
    fun observeMessages(familyId: String, roomId: String): Flow<List<LocalMessageEntity>>

    @Query("SELECT * FROM local_messages WHERE id = :msgId LIMIT 1")
    suspend fun getMessage(msgId: String): LocalMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(entity: LocalMessageEntity)

    @Query("SELECT * FROM local_moments WHERE familyId = :familyId ORDER BY createdAtMs DESC")
    fun observeMoments(familyId: String): Flow<List<LocalMomentEntity>>

    @Query("SELECT * FROM local_moments WHERE id = :momentId LIMIT 1")
    suspend fun getMoment(momentId: String): LocalMomentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMoment(entity: LocalMomentEntity)

    @Query("SELECT * FROM local_moment_comments WHERE momentId = :momentId ORDER BY createdAtMs ASC")
    fun observeMomentComments(momentId: String): Flow<List<LocalMomentCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMomentComment(entity: LocalMomentCommentEntity)

    @Query("SELECT * FROM local_calls WHERE familyId = :familyId ORDER BY createdAtMs DESC")
    fun observeCalls(familyId: String): Flow<List<LocalCallEntity>>

    @Query("SELECT * FROM local_calls WHERE id = :callId LIMIT 1")
    suspend fun getCall(callId: String): LocalCallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCall(entity: LocalCallEntity)

    @Query("SELECT * FROM local_tree_nodes WHERE familyId = :familyId ORDER BY generation ASC, fullName ASC")
    fun observeTreeNodes(familyId: String): Flow<List<LocalTreeNodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTreeNode(entity: LocalTreeNodeEntity)

    @Query("SELECT * FROM local_calendar_events WHERE familyId = :familyId ORDER BY eventDate ASC")
    fun observeCalendarEvents(familyId: String): Flow<List<LocalCalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarEvent(entity: LocalCalendarEventEntity)

    @Query("SELECT * FROM local_gallery_albums WHERE familyId = :familyId ORDER BY year DESC")
    fun observeGalleryAlbums(familyId: String): Flow<List<LocalGalleryAlbumEntity>>

    @Query("SELECT * FROM local_gallery_albums WHERE id = :albumId LIMIT 1")
    suspend fun getGalleryAlbum(albumId: String): LocalGalleryAlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGalleryAlbum(entity: LocalGalleryAlbumEntity)

    @Query("SELECT * FROM local_fund_transactions WHERE familyId = :familyId ORDER BY createdAtMs DESC")
    fun observeFundTransactions(familyId: String): Flow<List<LocalFundTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFundTransaction(entity: LocalFundTransactionEntity)

    @Query("DELETE FROM local_fund_transactions WHERE familyId = :familyId AND id = :txId")
    suspend fun deleteFundTransaction(familyId: String, txId: String)

    @Query("SELECT * FROM local_safety_alerts WHERE familyId = :familyId ORDER BY createdAtMs DESC")
    fun observeSafetyAlerts(familyId: String): Flow<List<LocalSafetyAlertEntity>>

    @Query("SELECT * FROM local_safety_alerts WHERE id = :alertId LIMIT 1")
    suspend fun getSafetyAlert(alertId: String): LocalSafetyAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSafetyAlert(entity: LocalSafetyAlertEntity)
}

@Database(
    entities = [
        LocalUserProfileEntity::class,
        LocalFamilyEntity::class,
        LocalMemberEntity::class,
        LocalGroupEntity::class,
        LocalPrivateChatEntity::class,
        LocalMessageEntity::class,
        LocalMomentEntity::class,
        LocalMomentCommentEntity::class,
        LocalCallEntity::class,
        LocalTreeNodeEntity::class,
        LocalCalendarEventEntity::class,
        LocalGalleryAlbumEntity::class,
        LocalFundTransactionEntity::class,
        LocalSafetyAlertEntity::class,
        LocalAccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FamilyLocalDatabase : RoomDatabase() {
    abstract fun familyDao(): FamilyLocalDao

    companion object {
        @Volatile
        private var INSTANCE: FamilyLocalDatabase? = null

        fun getInstance(context: Context): FamilyLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FamilyLocalDatabase::class.java,
                    "my_family_chat_local_trial.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
