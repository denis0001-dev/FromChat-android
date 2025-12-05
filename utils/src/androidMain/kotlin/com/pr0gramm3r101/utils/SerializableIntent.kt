@file:Suppress("MemberVisibilityCanBePrivate")

package com.pr0gramm3r101.utils

import android.content.ClipData
import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serial
import java.io.Serializable

fun Intent.serialize(outputStream: OutputStream) = SerializableIntent.serialize(this, outputStream)
fun Intent.toSerializableIntent() = SerializableIntent(this)

class SerializableIntent(): Intent(), Serializable {
    companion object {
        @Serial
        const val serialVersionUID = 1241895592857245245L

        fun deserialize(inputStream: InputStream): Intent {
            ObjectInputStream(inputStream).use {
                val obj = it.readObject() as SerializableIntent
                return obj.toIntent()
            }
        }

        fun serialize(intent: Intent, outputStream: OutputStream) {
            SerializableIntent(intent).serialize(outputStream)
        }
    }

    constructor(intent: Intent): this() {
        _data = intent.data
        _package = intent.`package`
        _flags = intent.flags
        _type = intent.type
        _action = intent.action
        _categories.addAll(intent.categories)
        _component = intent.component
        _extras = intent.extras
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            _identifier = intent.identifier
        }
        _sourceBounds = intent.sourceBounds
    }

    @Transient
    var _data: Uri? = null
    private var __data: String? = null
    var _package: String? = null
    var _flags: Int = 0
    var _type: String? = null
    val _scheme get() = _data?.scheme
    var _action: String? = null
    val _categories = mutableSetOf<String>()
    @Transient
    var _component: ComponentName? = null
    private var __component_packageName: String? = null
    private var __component_className: String? = null
    @Transient
    var _extras: Bundle? = null
    private var __extras_persistableBundle: PersistableBundle? = null
    var _identifier: String? = null
    @Transient
    var _sourceBounds: Rect? = null
    private var __sourceBounds_top: Int? = null
    private var __sourceBounds_bottom: Int? = null
    private var __sourceBounds_left: Int? = null
    private var __sourceBounds_right: Int? = null

    fun prepareSerialize() {
        __data = _data?.toString()
        __extras_persistableBundle = _extras?.toPersistableBundle()
        __component_packageName = _component?.packageName
        __component_className = _component?.className
        __sourceBounds_top = _sourceBounds?.top
        __sourceBounds_bottom = _sourceBounds?.bottom
        __sourceBounds_left = _sourceBounds?.left
        __sourceBounds_right = _sourceBounds?.right
    }

    fun serialize(outputStream: OutputStream) {
        prepareSerialize()
        ObjectOutputStream(outputStream).use {
            it.writeObject(this@SerializableIntent)
        }
    }

    fun toIntent(): Intent {
        val intent = Intent()
        intent.setDataAndType(_data, _type)
        intent.`package` = _package
        intent.flags = _flags
        intent.action = _action
        _categories.forEach {
            intent.addCategory(it)
        }
        intent.component = _component
        _extras?.let { intent.putExtras(it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.identifier = _identifier
        }
        _sourceBounds = Rect(
            __sourceBounds_left?: 0,
            __sourceBounds_top?: 0,
            __sourceBounds_right?: 0,
            __sourceBounds_bottom?: 0
        )
        return intent
    }

    // OVERRIDES
    override fun getData() = _data
    override fun setData(uri: Uri?): Intent {
        this._data = uri
        return this
    }

    override fun getPackage() = _package
    override fun setPackage(pkg: String?): Intent {
        this._package = pkg
        return this
    }
    override fun getFlags() = _flags
    override fun setFlags(flags: Int): Intent {
        this._flags = flags
        return this
    }

    override fun addFlags(flags: Int): Intent {
        this._flags = this._flags or flags
        return this
    }

    override fun getType() = _type
    override fun setType(type: String?): Intent {
        this._type = type
        return this
    }

    override fun getScheme() = _scheme

    override fun getAction() = _action
    override fun setAction(action: String?): Intent {
        this._action = action
        return this
    }

    override fun addCategory(category: String): Intent {
        _categories.add(category)
        return this
    }
    override fun getCategories() = _categories
    override fun hasCategory(category: String) = _categories.contains(category)
    override fun removeCategory(category: String) {
        _categories.remove(category)
    }

    override fun getClipData() = throw UnsupportedOperationException("Not supported in SerializableIntent")
    override fun setClipData(clipData: ClipData?) = throw UnsupportedOperationException("Not supported in SerializableIntent")

    override fun getComponent(): ComponentName? = _component
    override fun setComponent(component: ComponentName?): Intent {
        this._component = component
        return this
    }

    override fun getDataString() = _data?.toString()

    override fun getExtras() = _extras
    override fun putExtras(extras: Bundle): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putAll(extras)
        return this
    }
    override fun putExtras(src: Intent): Intent {
        if (src.extras != null) {
            putExtras(src.extras!!)
        }
        return this
    }
    override fun putExtra(name: String, value: Array<out CharSequence>?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putCharSequenceArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Array<out Parcelable>?): Intent {
        return this // Stub!
    }
    override fun putExtra(name: String, value: Array<out String>?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putStringArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Boolean): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putBoolean(name, value)
        return this
    }
    override fun putExtra(name: String, value: BooleanArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putBooleanArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Bundle?): Intent {
        return this // Stub!
    }
    override fun putExtra(name: String, value: Byte): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putByte(name, value)
        return this
    }
    override fun putExtra(name: String, value: ByteArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putByteArray(name, value)
        return this
    }
    override fun putExtra(name: String?, value: Char): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putChar(name, value)
        return this
    }
    override fun putExtra(name: String, value: CharArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putCharArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: CharSequence?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putCharSequence(name, value)
        return this
    }
    override fun putExtra(name: String, value: Double): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putDouble(name, value)
        return this
    }
    override fun putExtra(name: String, value: DoubleArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putDoubleArray(name, value)
        return this
    }
    override fun putExtra(name: String?, value: Float): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putFloat(name, value)
        return this
    }
    override fun putExtra(name: String, value: FloatArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putFloatArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Int): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putInt(name, value)
        return this
    }
    override fun putExtra(name: String, value: IntArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putIntArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Long): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putLong(name, value)
        return this
    }
    override fun putExtra(name: String, value: LongArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putLongArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: Parcelable?): Intent {
        return this // Stub!
    }
    override fun putExtra(name: String, value: Short): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putShort(name, value)
        return this
    }
    override fun putExtra(name: String, value: ShortArray?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putShortArray(name, value)
        return this
    }
    override fun putExtra(name: String, value: String?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putString(name, value)
        return this
    }
    override fun putExtra(name: String, value: Serializable?): Intent {
        return this // Stub!
    }
    override fun putStringArrayListExtra(name: String, value: ArrayList<String>?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putStringArrayList(name, value)
        return this
    }
    override fun putIntegerArrayListExtra(name: String, value: ArrayList<Int>?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putIntegerArrayList(name, value)
        return this
    }
    override fun putParcelableArrayListExtra(name: String?, value: java.util.ArrayList<out Parcelable>?): Intent {
        return this // Stub!
    }
    override fun putCharSequenceArrayListExtra(name: String?, value: java.util.ArrayList<CharSequence>?): Intent {
        if (this._extras == null) {
            this._extras = Bundle()
        }
        this._extras!!.putCharSequenceArrayList(name, value)
        return this
    }
    override fun getBooleanExtra(name: String, defaultValue: Boolean): Boolean {
        return _extras?.getBoolean(name, defaultValue) ?: defaultValue
    }
    override fun getBooleanArrayExtra(name: String): BooleanArray? {
        return _extras?.getBooleanArray(name)
    }
    override fun getByteExtra(name: String, defaultValue: Byte): Byte {
        return _extras?.getByte(name, defaultValue) ?: defaultValue
    }
    override fun getByteArrayExtra(name: String?): ByteArray? {
        return _extras?.getByteArray(name)
    }
    override fun getCharExtra(name: String, defaultValue: Char): Char {
        return _extras?.getChar(name, defaultValue) ?: defaultValue
    }
    override fun getCharArrayExtra(name: String?): CharArray? {
        return _extras?.getCharArray(name)
    }
    override fun getDoubleExtra(name: String, defaultValue: Double): Double {
        return _extras?.getDouble(name, defaultValue) ?: defaultValue
    }
    override fun getDoubleArrayExtra(name: String?): DoubleArray? {
        return _extras?.getDoubleArray(name)
    }
    override fun getFloatExtra(name: String, defaultValue: Float): Float {
        return _extras?.getFloat(name, defaultValue) ?: defaultValue
    }
    override fun getFloatArrayExtra(name: String?): FloatArray? {
        return _extras?.getFloatArray(name)
    }
    override fun getIntExtra(name: String, defaultValue: Int): Int {
        return _extras?.getInt(name, defaultValue) ?: defaultValue
    }
    override fun getIntArrayExtra(name: String?): IntArray? {
        return _extras?.getIntArray(name)
    }
    override fun getLongExtra(name: String, defaultValue: Long): Long {
        return _extras?.getLong(name, defaultValue) ?: defaultValue
    }
    override fun getLongArrayExtra(name: String): LongArray? {
        return _extras?.getLongArray(name)
    }
    @Deprecated("Deprecated in Java")
    override fun getSerializableExtra(name: String): Serializable? {
        return null // Stub!
    }
    override fun getStringArrayListExtra(name: String): ArrayList<String>? {
        return _extras?.getStringArrayList(name)
    }
    override fun getIntegerArrayListExtra(name: String): ArrayList<Int>? {
        return _extras?.getIntegerArrayList(name)
    }
    override fun getCharSequenceArrayListExtra(name: String): ArrayList<CharSequence>? {
        return _extras?.getCharSequenceArrayList(name)
    }
    override fun <T : Serializable?> getSerializableExtra(name: String?, clazz: Class<T>): T? {
        return null // Stub!
    }
    override fun getBundleExtra(name: String): Bundle? {
        return null // Stub!
    }
    override fun getStringExtra(name: String): String? {
        return _extras?.getString(name)
    }
    override fun getShortExtra(name: String, defaultValue: Short): Short {
        return _extras?.getShort(name, defaultValue) ?: defaultValue
    }
    override fun getShortArrayExtra(name: String): ShortArray? {
        return _extras?.getShortArray(name)
    }
    override fun getStringArrayExtra(name: String): Array<String>? {
        return _extras?.getStringArray(name)
    }
    override fun getCharSequenceExtra(name: String): CharSequence? {
        return _extras?.getCharSequence(name)
    }
    override fun <T : Any?> getParcelableExtra(name: String?, clazz: Class<T>): T? {
        return null // Stub!
    }
    @Deprecated("Deprecated in Java")
    override fun <T : Parcelable?> getParcelableExtra(name: String?): T? {
        return null // Stub!
    }
    override fun getCharSequenceArrayExtra(name: String): Array<CharSequence>? {
        return _extras?.getCharSequenceArray(name)
    }
    override fun <T : Any?> getParcelableArrayExtra(name: String?, clazz: Class<T>): Array<T>? {
        return null // Stub!
    }
    @Deprecated("Deprecated in Java")
    override fun getParcelableArrayExtra(name: String): Array<Parcelable>? {
        return null // Stub!
    }
    override fun <T : Any?> getParcelableArrayListExtra(name: String?, clazz: Class<out T>): java.util.ArrayList<T>? {
        return null // Stub!
    }
    @Deprecated("Deprecated in Java")
    override fun <T : Parcelable?> getParcelableArrayListExtra(name: String?): java.util.ArrayList<T>? {
        return null // Stub!
    }

    override fun getIdentifier() = _identifier
    override fun setIdentifier(identifier: String?): Intent {
        this._identifier = identifier
        return this
    }

    override fun getSelector(): Intent? {
        return null
    }
    override fun setSelector(selector: Intent?) {
        // Stub!
    }

    override fun getSourceBounds() = _sourceBounds
    override fun setSourceBounds(sourceBounds: Rect?) {
        this._sourceBounds = sourceBounds
    }

    override fun isMismatchingFilter() = false
}