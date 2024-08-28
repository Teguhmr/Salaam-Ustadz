package id.uinjkt.salaamustadz.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import java.util.Arrays


object SharedUtils {
    private lateinit var settings: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    fun saveArrayList(context: Context, key: String, unread_ids: List<Int>) {
        settings = context.getSharedPreferences(
            "shared_pref",
            Context.MODE_PRIVATE
        )
        editor = settings.edit()
        val gson = Gson()
        val jsonFavorites = gson.toJson(unread_ids)
        editor.putString(key, jsonFavorites)
        editor.apply()
    }

    fun getSavedList(context: Context, key: String): ArrayList<Int> {
        // SharedPreferences settings;
        var unReadId: List<Int>
        settings = context.getSharedPreferences(
            "shared_pref",
            Context.MODE_PRIVATE
        )
        if (settings.contains(key)) {
            val jsonFavorites = settings.getString(key, "")
            val gson = Gson()
            val favoriteItems = gson.fromJson(
                jsonFavorites,
                Array<Int>::class.java
            )
            unReadId = Arrays.asList(*favoriteItems)
            unReadId = ArrayList(unReadId)
        } else {
            return ArrayList()
        }
        return unReadId
    }
    fun clearList(context: Context){
        settings = context.getSharedPreferences(
            "shared_pref",
            Context.MODE_PRIVATE
        )
        editor = settings.edit()
        editor.clear()
        editor.apply()
    }

    fun removeSingleValueString(context: Context, key: String) {
        settings = context.getSharedPreferences(
            "shared_pref",
            Context.MODE_PRIVATE
        )
        editor = settings.edit()
        editor.remove(key)
        editor.apply()
    }
}