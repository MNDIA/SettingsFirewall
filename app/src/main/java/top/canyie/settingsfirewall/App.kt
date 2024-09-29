package top.canyie.settingsfirewall

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.RemoteException
import android.provider.Settings
import android.provider.Settings.NameValueTable
import android.provider.Settings.Secure
import android.text.TextUtils
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Modifier
import java.util.Arrays
import java.util.Collections

/**
 * @author canyie
 */
class App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) HiddenApiBypass.addHiddenApiExemptions("")
    }

    companion object {
        val MY_UID: Int = Process.myUid()
        private var service: ISettingsFirewall? = null

        fun getService(context: Context): ISettingsFirewall? {
            if (service == null) {
                val uri = Uri.parse("content://" + Settings.AUTHORITY)
                try {
                    val result = context.contentResolver.call(
                        uri, SettingsProviderHook.Companion.METHOD,
                        null, null
                    )
                    if (result != null) service = ISettingsFirewall.Stub.asInterface(
                        result.getBinder(
                            NameValueTable.VALUE
                        )
                    )
                } catch (ignored: Exception) {
                }
            }
            return service
        }

        fun getSortedList(context: Context, service: ISettingsFirewall): List<AppInfo> {
            val pm = context.packageManager
            val apps = pm.getInstalledPackages(0)
            val enabledUids: IntArray
            try {
                enabledUids = service.targets
            } catch (e: RemoteException) {
                throw RuntimeException(e)
            }
            Arrays.sort(enabledUids)
            val list: MutableList<AppInfo> = ArrayList()
            val addedSharedUserIds: MutableSet<String> = HashSet()
            var androidIcon: Drawable? = null
            for (app in apps) {
                val appInfo = app.applicationInfo
                val uid = appInfo.uid
                // Skip system core process and ourselves
                if (uid < Process.SHELL_UID) continue
                if (uid == MY_UID) continue
                var info: AppInfo
                if (TextUtils.isEmpty(app.sharedUserId)) {
                    info = AppInfo()
                    info.name = appInfo.loadLabel(pm).toString()
                    info.icon = appInfo.loadIcon(pm)
                } else {
                    val sharedUserId = app.sharedUserId
                    if (!addedSharedUserIds.add(sharedUserId)) continue
                    info = AppInfo()
                    info.name = "[SharedUserID] $sharedUserId"
                    if (androidIcon == null) androidIcon =
                        context.resources.getDrawable(android.R.mipmap.sym_def_app_icon)
                    info.icon = androidIcon
                    info.isSharedUid = true
                }
                info.enabled = Arrays.binarySearch(enabledUids, uid.also { info.uid = it }) >= 0
                list.add(info)
            }
            Collections.sort<AppInfo>(list, AppInfo.Companion.COMPARATOR)
            return list
        }

        fun getSettings(service: ISettingsFirewall?, uid: Int): List<Replacement> {
            val replacements: Array<Replacement>
            try {
                replacements = service!!.getReplacements(uid)
            } catch (e: RemoteException) {
                // Should never happen: the remote service runs in Settings Provider which is in system_server
                throw RuntimeException(e)
            }
            val out: MutableList<Replacement> = ArrayList()
            addSettings(
                Settings.System::class.java, out, Replacement.Companion.FLAG_SYSTEM, replacements,
                "MOVED_TO_SECURE", "MOVED_TO_GLOBAL", "MOVED_TO_SECURE_THEN_GLOBAL"
            )
            addSettings(
                Secure::class.java, out, Replacement.Companion.FLAG_SECURE, replacements,
                "MOVED_TO_GLOBAL"
            )
            addSettings(
                Settings.Global::class.java,
                out,
                Replacement.Companion.FLAG_GLOBAL,
                replacements
            )
            Collections.sort<Replacement>(out, Replacement.Companion.COMPARATOR)
            return out
        }

        private fun addSettings(
            cls: Class<out NameValueTable?>,
            out: MutableList<Replacement>, flag: Int, replacements: Array<Replacement>?,
            vararg ignore: String
        ) {
            val ignoreSets: Array<Set<String?>> = arrayOfNulls<Set<*>>(ignore.size)
            for (i in ignore.indices) {
                var set: Set<String?>
                try {
                    val field = cls.getDeclaredField(ignore[i])
                    field.isAccessible = true
                    set = field[null] as Set<String?>
                } catch (e: Exception) {
                    Log.w("SettingsFirewall", "Unable to access " + cls + "." + ignore[i], e)
                    set = emptySet<String>()
                }
                ignoreSets[i] = set
            }
            val fields = cls.declaredFields
            outer@ for (field in fields) {
                val modifiers = field.modifiers
                if (!(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))) continue
                if (field.type != String::class.java) continue
                field.isAccessible = true
                var key: String
                try {
                    key = field[null] as String
                } catch (e: Exception) {
                    // Should never happen
                    throw RuntimeException(e)
                }
                if (TextUtils.isEmpty(key)) continue
                for (set in ignoreSets) if (set.contains(key)) continue@outer
                if (replacements != null) {
                    for (existing in replacements) {
                        if (key == existing.key) {
                            out.add(existing)
                            continue@outer
                        }
                    }
                }
                out.add(Replacement(key, null, flag))
            }
        }
    }
}
