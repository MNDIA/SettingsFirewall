package top.canyie.settingsfirewall

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings.NameValueTable
import android.util.SparseArray
import de.robv.android.xposed.XposedBridge
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * @author canyie
 */
class SettingsFirewallService private constructor() : ISettingsFirewall.Stub() {
    override fun getTargets(): IntArray {
        readLock.lock()
        try {
            val N = targetUids.size
            val a = IntArray(N)
            var i = 0
            for (uid in targetUids) a[i++] = uid
            return a
        } finally {
            readLock.unlock()
        }
    }

    override fun setTarget(uid: Int, enabled: Boolean) {
        writeLock.lock()
        try {
            if (enabled) targetUids.add(uid)
            else targetUids.remove(uid)
            saveTargets(targetUids)
        } finally {
            writeLock.unlock()
        }
    }

    override fun getReplacements(uid: Int): Array<Replacement> {
        readLock.lock()
        try {
            val list: List<Replacement>? = policyCache[uid]
            // Deep-copy the list before releasing the lock, to avoid the content being changed
            // in a time window between returning(unlocking) and writing its content into parcel
            return list?.toTypedArray<Replacement>()
        } finally {
            readLock.unlock()
        }
    }

    override fun setReplacement(uid: Int, setting: String, value: String, flags: Int) {
        writeLock.lock()
        try {
            var replacements = policyCache[uid]
            if (replacements == null) {
                policyCache.put(uid, ArrayList<Replacement>().also { replacements = it })
            }
            var add = true
            for (replacement in replacements!!) {
                if (setting == replacement.key) {
                    replacement.value = value
                    replacement.flags = flags
                    add = false
                }
            }
            if (add) replacements!!.add(Replacement(setting, value, flags))
            saveUidRules(uid, replacements)
        } finally {
            writeLock.unlock()
        }
    }

    override fun deleteReplacement(uid: Int, setting: String) {
        writeLock.lock()
        try {
            val replacements = policyCache[uid] ?: return
            val iterator = replacements.iterator()
            while (iterator.hasNext()) {
                val replacement = iterator.next()
                if (setting == replacement.key) {
                    iterator.remove()
                }
            }
            saveUidRules(uid, replacements)
        } finally {
            writeLock.unlock()
        }
    }

    companion object {
        val INSTANCE: SettingsFirewallService = SettingsFirewallService()
        val BUNDLE: Bundle = Bundle(1)

        private const val KEY_TARGET_UIDS = "firewall_targets"
        private const val FILENAME = "settings-firewall"
        private var policyDir: File? = null
        private var sharedPreferences: SharedPreferences? = null
        private val targetUids: MutableSet<Int> = HashSet()
        private val policyCache = SparseArray<MutableList<Replacement>>()
        private val readLock: Lock
        private val writeLock: Lock

        init {
            BUNDLE.putBinder(NameValueTable.VALUE, INSTANCE.asBinder())
            val lock = ReentrantReadWriteLock()
            readLock = lock.readLock()
            writeLock = lock.writeLock()
        }

        fun init(context: Context?) {
            var context = context
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context =
                context!!.createDeviceProtectedStorageContext()
            sharedPreferences = context!!.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)
            val uidSet = sharedPreferences.getStringSet(KEY_TARGET_UIDS, null)
            if (uidSet != null) {
                for (uid in uidSet) {
                    try {
                        targetUids.add(uid.toInt())
                    } catch (e: NumberFormatException) {
                        XposedBridge.log("[SettingsFirewall] Found invalid target uid $uid")
                    }
                }
            }
            policyDir = context.getDir(FILENAME, Context.MODE_PRIVATE)
            XposedBridge.log("[SettingsFirewall] Load uid policy from " + policyDir)
            val files = policyDir.listFiles() ?: return
            for (file in files) {
                var uid: Int
                val filename = file.name
                try {
                    uid = filename.toInt()
                } catch (e: NumberFormatException) {
                    XposedBridge.log("[SettingsFirewall] Found invalid file $file")
                    continue
                }
                try {
                    ObjectInputStream(FileInputStream(file)).use { `in` ->
                        val o = `in`.readObject()
                        policyCache.put(uid, o as MutableList<Replacement>)
                    }
                } catch (e: Exception) {
                    // Maybe we should delete the file, since the file may be corrupted?
                    XposedBridge.log("[SettingsFirewall] Error reading $file")
                    XposedBridge.log(e)
                }
            }
        }

        fun getReplacement(uid: Int, setting: String, flag: Int): String? {
            readLock.lock()
            try {
                if (!targetUids.contains(uid)) return null
                val replacements: List<Replacement>? = policyCache[uid]
                if (replacements != null) for (replacement in replacements) if (setting == replacement.key && (replacement.flags and flag) != 0) return replacement.value
            } finally {
                readLock.unlock()
            }
            return null
        }

        private fun saveTargets(uids: Set<Int>) {
            val stringSet: MutableSet<String> = HashSet(uids.size, 2f)
            for (uid in uids) {
                stringSet.add(uid.toString())
            }
            sharedPreferences!!.edit().putStringSet(KEY_TARGET_UIDS, stringSet).commit()
        }

        private fun saveUidRules(uid: Int, replacements: List<Replacement>?) {
            try {
                ObjectOutputStream(
                    FileOutputStream(
                        File(
                            policyDir, uid.toString()
                        )
                    )
                ).use { out ->
                    out.writeObject(replacements)
                }
            } catch (e: IOException) {
                XposedBridge.log("[SettingsFirewall] Error saving rules of uid $uid")
                XposedBridge.log(e)
            }
        }
    }
}
