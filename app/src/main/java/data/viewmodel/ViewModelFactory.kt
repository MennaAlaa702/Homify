package data.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import data.dao.UserDao
import data.dao.UnitDao
import data.dao.ProfileDao

class ViewModelFactory(
    private val application: Application,
    private val userDao: UserDao? = null,
    private val unitDao: UnitDao? = null,
    private val profileDao: ProfileDao? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // لو المطلوب UserViewModel، اديله الـ UserDao والـ ProfileDao
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(userDao!!, profileDao!!, application) as T
            }
            // لو المطلوب UnitViewModel، اديله الـ UnitDao
            modelClass.isAssignableFrom(UnitViewModel::class.java) -> {
                UnitViewModel(unitDao!!, application) as T
            }
            // لو المطلوب AdminViewModel، اديله الـ UserDao والـ UnitDao
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(userDao!!, unitDao!!, application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}