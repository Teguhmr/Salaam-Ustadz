package id.uinjkt.salaamustadz.data.preferences

import android.content.Context
import android.content.SharedPreferences
import id.uinjkt.salaamustadz.data.auth.UserSession


class UserLoginPreferences(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = pref.edit()

    fun setUserLogin(user: UserSession){
        editor.apply{
            putString(NAME_KEY, user.name)
            putString(TOKEN_KEY, user.token)
            putString(USER_ID_KEY, user.userId)
            putBoolean(STATE_KEY, user.isLogin)
            apply()
        }
    }

    fun setName(user: String){
        editor.apply{
            putString(NAME_KEY, user)
            apply()
        }
    }
    fun getName(): String? {
        return pref.getString(NAME_KEY, "")
    }

    fun storeDataArrayList(stringList: List<String>?) {
        val set: MutableSet<String> = HashSet()
        set.addAll(stringList!!)
        editor.putStringSet(KEY_ARRAY_LIST, set)
        editor.apply()
    }

    /*
        2. Serialize your ArrayList and then read it from SharedPreferences.
     */
    fun getDataArrayList(): List<String?> {
        val set = pref.getStringSet(KEY_ARRAY_LIST, null)
        var list: List<String?> = ArrayList()
        if (set != null) {
            list = ArrayList(set)
        }
        return list
    }

    fun logout(){
        editor.apply {
            remove(NAME_KEY)
            remove(TOKEN_KEY)
            remove(USER_ID_KEY)
            putBoolean(STATE_KEY, false)
            apply()
        }
    }

    fun getLoginData(): UserSession {
        val loggedUser = UserSession(
            pref.getString(NAME_KEY, "").toString(),
            pref.getString(TOKEN_KEY, "").toString(),
            pref.getString(USER_ID_KEY, "").toString(),
            pref.getBoolean(STATE_KEY, false)
        )
        return loggedUser
    }

    fun getStatusLogin(): Boolean {
        return pref.getBoolean(STATE_KEY, false)
    }

    companion object{
        private const val PREF_NAME = "USER_LOGIN"
        private const val NAME_KEY = "NAME"
        private const val TOKEN_KEY = "TOKEN"
        private const val USER_ID_KEY = "USER_ID"
        private const val STATE_KEY = "STATE"
        private const val KEY_ARRAY_LIST = "arrayListData"


    }
}