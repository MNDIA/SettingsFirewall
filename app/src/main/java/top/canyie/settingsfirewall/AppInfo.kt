package top.canyie.settingsfirewall

import android.graphics.drawable.Drawable

/**
 * @author canyie
 */
class AppInfo {
    var uid: Int = 0
    var name: String? = null
    var icon: Drawable? = null
    var enabled: Boolean = false
    var isSharedUid: Boolean = false

    override fun equals(obj: Any?): Boolean {
        return obj is AppInfo && uid == obj.uid
    }

    override fun hashCode(): Int {
        return uid
    }

    companion object {
        val COMPARATOR: Comparator<AppInfo> = Comparator { a: AppInfo, b: AppInfo ->
            if (a.enabled != b.enabled) return@Comparator if (a.enabled) -1 else 1
            if (a.isSharedUid != b.isSharedUid) return@Comparator if (a.isSharedUid) -1 else 1
            a.name!!.compareTo(b.name!!)
        }
    }
}
