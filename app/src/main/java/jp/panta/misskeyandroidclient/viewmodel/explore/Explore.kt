package jp.panta.misskeyandroidclient.viewmodel.explore

import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.viewmodel.tags.SortedHashTagListViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.SortedUsersViewModel
import java.io.Serializable

sealed class Explore(open val name: String): Serializable{

    data class Tag(
        override val name: String,
        val conditions: SortedHashTagListViewModel.Conditions
    ): Explore(name)

    data class User(
        override val name: String,
        val origin: RequestUser.Origin?,
        val sort: String?,
        val state: RequestUser.State?
    ): Explore(name)

    data class UserType(
        override val name: String,
        val type: SortedUsersViewModel.Type
    ): Explore(name)
}