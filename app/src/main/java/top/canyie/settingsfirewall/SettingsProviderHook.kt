package top.canyie.settingsfirewall

import android.content.ContentProvider
import android.content.Context
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.provider.Settings.NameValueTable
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.concurrent.Volatile

/**
 * @author canyie
 */
class SettingsProviderHook : XC_MethodHook(), IXposedHookLoadPackage {
    @Volatile
    private var context: Context? = null

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if ("com.android.providers.settings" != lpparam.packageName) return
        XposedHelpers.findAndHookMethod(
            "com.android.providers.settings.SettingsProvider",
            lpparam.classLoader,
            "call",
            String::class.java,
            String::class.java,
            Bundle::class.java,
            this
        )
    }

    override fun beforeHookedMethod(param: MethodHookParam) {
        val contentProvider = param.thisObject as ContentProvider
        val method = param.args[0] as String
        val name = param.args[1] as String
        //        Bundle args = (Bundle) param.args[2];
        if (context == null) {
            synchronized(this) {
                if (context == null) {
                    context = contentProvider.context
                    SettingsFirewallService.Companion.init(context)
                }
            }
        }
        val callingUid = Binder.getCallingUid()
        val flag: Int
        when (method) {
            METHOD -> {
                // Verify the calling package is actually our module. If not, don't send anything.
                // We catch all possible exception to make sure unauthorized apps can't fool us.
                // For example, if the caller tries to lie us that it is another package,
                // getCallingPackage will throw an exception but we avoid delivering the exception
                // back to the caller because it is a side channel and can be detected
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && BuildConfig.APPLICATION_ID != contentProvider.callingPackage
                    ) return
                    param.result = SettingsFirewallService.Companion.BUNDLE
                } catch (ignored: Exception) {
                }
                return
            }

            "GET_global" -> flag = Replacement.Companion.FLAG_GLOBAL
            "GET_secure" -> flag = Replacement.Companion.FLAG_SECURE
            "GET_system" -> flag = Replacement.Companion.FLAG_SYSTEM
            else -> return
        }
        val replacement: String? =
            SettingsFirewallService.Companion.getReplacement(callingUid, name, flag)
        if (replacement != null) {
            val result = Bundle(1)
            result.putString(NameValueTable.VALUE, replacement)
            param.result = result
        }
    }

    companion object {
        const val METHOD: String = "GET_SettingsFirewall"
    }
}
