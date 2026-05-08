package data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import data.dao.UserDao
import data.dao.UnitDao
import data.entities.User
import data.entities.Unit
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AdminViewModel(
    private val userDao: UserDao,
    private val unitDao: UnitDao,
    application: Application
) : AndroidViewModel(application) {

    // 1. الإحصائيات للـ Home (بتتحدث تلقائياً عند أي تغيير)
    fun getSystemStats(): LiveData<Map<String, Int>> {
        return combine(
            userDao.getTotalUsersCount(),
            userDao.getTotalTenantsCount(),
            userDao.getTotalLandlordsCount(),
            unitDao.getTotalUnitsCount()
        ) { users, tenants, landlords, units ->
            mapOf(
                "totalUsers"     to users,
                "totalTenants"   to tenants,
                "totalLandlords" to landlords,
                "totalUnits"     to units
            )
        }.asLiveData()
    }

    // 2. إدارة المستخدمين
    val allUsers: LiveData<List<User>> = userDao.getAllUsersForAdmin().asLiveData()

    fun removeUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }

    // 3. إدارة الوحدات
    val allUnits: LiveData<List<Unit>> = unitDao.getAllUnitsForAdmin().asLiveData()

    fun removeUnit(unit: Unit) {
        viewModelScope.launch {
            unitDao.deleteUnit(unit)
        }
    }

    fun removeUnitById(unitId: Int) {
        viewModelScope.launch {
            unitDao.deleteUnitById(unitId)
        }
    }

    fun removeUserById(userId: Int) {
        viewModelScope.launch {
            userDao.deleteUserById(userId)
        }
    }
}