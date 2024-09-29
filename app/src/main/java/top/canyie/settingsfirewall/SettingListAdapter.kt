package top.canyie.settingsfirewall

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

/**
 * @author canyie
 */
class SettingListAdapter(private val activity: SettingsEditActivity, list: List<Replacement>?) :
    ArrayAdapter<Replacement?>(
        activity, 0, list!!
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val replacement = checkNotNull(getItem(position))
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.setting_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.key = convertView.findViewById(R.id.key)
            viewHolder.replacement = convertView.findViewById(R.id.replacement)
            convertView.setTag(viewHolder)
        } else {
            viewHolder = convertView.tag as ViewHolder
        }
        convertView!!.setOnClickListener { v: View? -> activity.onItemClicked(replacement) }
        viewHolder.key!!.text = replacement.key
        if (replacement.value != null) {
            viewHolder.replacement!!.visibility = View.VISIBLE
            viewHolder.replacement!!.text =
                context.getString(R.string.replaced_with, replacement.value)
        } else {
            viewHolder.replacement!!.visibility = View.GONE
        }
        return convertView
    }

    private class ViewHolder {
        var key: TextView? = null
        var replacement: TextView? = null
    }
}
