package top.canyie.settingsfirewall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView

/**
 * @author canyie
 */
class AppListAdapter(private val activity: MainActivity, list: List<AppInfo>?) :
    ArrayAdapter<AppInfo?>(
        activity, 0, list!!
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val appInfo = checkNotNull(getItem(position))
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.app_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.name = convertView.findViewById(R.id.app_name)
            viewHolder.icon = convertView.findViewById(R.id.app_icon)
            viewHolder.checkBox = convertView.findViewById(R.id.checkbox)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        convertView!!.setOnClickListener { v: View? -> activity.onItemClicked(appInfo) }
        viewHolder.name!!.text = appInfo.name
        viewHolder.icon!!.setImageDrawable(appInfo.icon)
        viewHolder.checkBox!!.setOnCheckedChangeListener(null)
        viewHolder.checkBox!!.isChecked = appInfo.enabled
        viewHolder.checkBox!!.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            activity.onItemChecked(
                appInfo,
                isChecked
            )
        }
        return convertView
    }

    private class ViewHolder {
        var name: TextView? = null
        var icon: ImageView? = null
        var checkBox: CheckBox? = null
    }
}
