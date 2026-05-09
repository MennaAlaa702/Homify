package data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import data.dao.UnitDao
import data.entities.Unit
import kotlinx.coroutines.launch

class UnitViewModel(
    private val unitDao: UnitDao,
    application: Application
) : AndroidViewModel(application) {

    // 1. عرض الوحدات للـ Tenant (حسب الترتيب)
    // الافتراضي: الأحدث
    val allUnitsLatest: LiveData<List<Unit>> = unitDao.getUnitsLatest().asLiveData()

    // الأرخص
    val allUnitsCheapest: LiveData<List<Unit>> = unitDao.getUnitsCheapest().asLiveData()

    // الأغلى
    val allUnitsMostExpensive: LiveData<List<Unit>> = unitDao.getUnitsMostExpensive().asLiveData()

    // الأبجدي
    val allUnitsAlphabetical: LiveData<List<Unit>> = unitDao.getUnitsAlphabetical().asLiveData()

    // 2. عرض وحدات الـ Landlord فقط
    fun getLandlordUnits(landlordId: Int): LiveData<List<Unit>> {
        return unitDao.getUnitsByLandlord(landlordId).asLiveData()
    }

    // 3. عدد وحدات المالك (للبروفايل والـ Home)
    fun getUnitCount(landlordId: Int): LiveData<Int> {
        val count = MutableLiveData<Int>()
        viewModelScope.launch {
            count.postValue(unitDao.getUnitCountForLandlord(landlordId))
        }
        return count
    }

    // 4. حذف وحدة (للأدمن أو المالك)
    fun deleteUnit(unit: Unit) {
        viewModelScope.launch {
            unitDao.deleteUnit(unit)
        }
    }
}