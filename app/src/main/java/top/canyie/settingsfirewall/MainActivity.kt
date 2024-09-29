package top.canyie.settingsfirewall

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.view.View
import android.widget.ListView

/**
 * @author canyie
 */
class MainActivity : Activity() {
    private var service: ISettingsFirewall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.main)
            findViewById<View>(android.R.id.home).visibility = View.GONE
            service = App.Companion.getService(this)
            findViewById<View>(R.id.progress_bar).visibility = View.GONE
            if (service == null) {
                findViewById<View>(R.id.not_activated_msg).visibility = View.VISIBLE
                return
            }
            val listView = findViewById<ListView>(R.id.list)
            val adapter = AppListAdapter(this, App.Companion.getSortedList(this, service!!))
            listView.adapter = adapter
            listView.visibility = View.VISIBLE
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun onItemChecked(appInfo: AppInfo, checked: Boolean) {
        try {
            appInfo.enabled = checked
            service!!.setTarget(appInfo.uid, checked)
        } catch (e: RemoteException) {
            throw RuntimeException(e)
        }
    }

    fun onItemClicked(appInfo: AppInfo) {
        startActivity(
            Intent(this, SettingsEditActivity::class.java)
                .putExtra(SettingsEditActivity.Companion.KEY_UID, appInfo.uid)
                .putExtra(SettingsEditActivity.Companion.KEY_NAME, appInfo.name)
        )
    }
}
