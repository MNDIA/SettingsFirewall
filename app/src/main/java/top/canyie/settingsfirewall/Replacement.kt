package top.canyie.settingsfirewall

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.io.Serial
import java.io.Serializable

/**
 * @author canyie
 */
class Replacement(val key: String?, var value: String?, var flags: Int) : Serializable, Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(key)
        dest.writeString(value)
        dest.writeInt(this.flags)
    }

    override fun equals(obj: Any?): Boolean {
        return obj is Replacement && key == obj.key && flags == obj.flags
    }

    override fun hashCode(): Int {
        return key.hashCode() xor flags
    }

    override fun toString(): String {
        return "Replacement{key=$key, value=$value, flags=$flags}"
    }

    companion object {
        const val FLAG_SYSTEM: Int = 1 shl 0
        const val FLAG_SECURE: Int = 1 shl 1
        const val FLAG_GLOBAL: Int = 1 shl 2

        @Serial
        private val serialVersionUID = 1145141919810L
        @JvmField
        val CREATOR: Creator<Replacement?> = object : Creator<Replacement?> {
            override fun createFromParcel(`in`: Parcel): Replacement? {
                return Replacement(`in`.readString(), `in`.readString(), `in`.readInt())
            }

            override fun newArray(size: Int): Array<Replacement?> {
                return arrayOfNulls(size)
            }
        }

        val COMPARATOR: Comparator<Replacement> = Comparator { a: Replacement, b: Replacement ->
            val aReplaced = a.value != null
            val bReplaced = b.value != null
            if (aReplaced != bReplaced) return@Comparator if (aReplaced) -1 else 1
            a.key!!.compareTo(b.key!!)
        }
    }
}
