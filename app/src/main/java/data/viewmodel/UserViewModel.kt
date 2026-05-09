package data.viewmodel // تأكدي إن المسار ده مطابق لمكان الفولدر عندك

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import data.dao.UserDao
import data.dao.ProfileDao
import data.entities.User
import data.entities.TenantProfile
import data.entities.LandlordProfile
import kotlinx.coroutines.launch

class UserViewModel(
    private val userDao: UserDao,
    private val profileDao: ProfileDao,
    application: Application
) : AndroidViewModel(application) {

    // 1. جلب بيانات المستخدم الأساسية (الاسم، الإيميل، الدور)
    fun getUserData(userId: Int): LiveData<User?> {
        val userResult = MutableLiveData<User?>()
        viewModelScope.launch {
            userResult.postValue(userDao.getUserById(userId))
        }
        return userResult
    }

    // 2. جلب بروفايل المستأجر (Phone, National ID)
    fun getTenantProfile(userId: Int): LiveData<TenantProfile?> {
        val profileResult = MutableLiveData<TenantProfile?>()
        viewModelScope.launch {
            profileResult.postValue(profileDao.getTenantByUserId(userId))
        }
        return profileResult
    }

    // 3. جلب بروفايل المالك (لو محتاجة بيانات خاصة به مستقبلاً)
    fun getLandlordProfile(userId: Int): LiveData<LandlordProfile?> {
        val profileResult = MutableLiveData<LandlordProfile?>()
        viewModelScope.launch {
            profileResult.postValue(profileDao.getLandlordByUserId(userId))
        }
        return profileResult
    }

    // 4. مسح مستخدم (خاص بالأدمن)
    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }

    //  حفظ مسار الصورة في الـ DB

    fun updateProfileImage(userId: Int, imagePath: String?) {
        viewModelScope.launch {
            userDao.updateProfileImagePath(userId, imagePath)
        }
    }

}