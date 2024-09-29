package top.canyie.settingsfirewall

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ListView

/**
 * @author canyie
 */
class SettingsEditActivity : Activity() {
    private var uid = 0
    private var service: ISettingsFirewall? = null
    private var adapter: SettingListAdapter? = null
    private var layoutInflater: LayoutInflater? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent == null || (intent.getIntExtra(KEY_UID, -1).also { uid = it }) == -1) {
            finish()
            return
        }
        setContentView(R.layout.settings)
        layoutInflater = LayoutInflater.from(this)
        val actionBar = actionBar
        actionBar!!.setDisplayShowHomeEnabled(false)
        actionBar.setTitle(R.string.edit_settings_replacement)
        actionBar.subtitle = intent.getStringExtra(KEY_NAME)
        service = App.Companion.getService(this)
        adapter = SettingListAdapter(this, App.Companion.getSettings(service, uid))
        val listView = findViewById<ListView>(R.id.list)
        listView.adapter = adapter
    }

    fun onItemClicked(replacement: Replacement) {
        val layout = layoutInflater!!.inflate(R.layout.edit_dialog, null)
        @SuppressLint("MissingInflatedId", "LocalSuppress") val editText =
            layout.findViewById<EditText>(R.id.edit)
        if (replacement.value != null) editText.setText(replacement.value)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.editing, replacement.key))
            .setView(layout)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save) { dialog: DialogInterface?, which: Int ->
                replacement.value = editText.text.toString()
                try {
                    service!!.setReplacement(
                        uid,
                        replacement.key,
                        replacement.value,
                        replacement.flags
                    )
                } catch (e: RemoteException) {
                    throw RuntimeException(e)
                }
                adapter!!.notifyDataSetChanged()
            }
            .setNeutralButton(R.string.delete) { dialog: DialogInterface?, which: Int ->
                replacement.value = null
                try {
                    service!!.deleteReplacement(uid, replacement.key)
                } catch (e: RemoteException) {
                    throw RuntimeException(e)
                }
                adapter!!.notifyDataSetChanged()
            }
            .setCancelable(false)
            .show()
    }

    companion object {
        const val KEY_NAME: String = "name"
        const val KEY_UID: String = "uid"
    }
}
