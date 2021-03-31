package posidon.android.loader.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doOnTextChanged

inline fun <T : Activity> ActivityButton(context: Context, name: String, activity: Class<T>): View {
    return TextView(context).apply {
        text = name
        textSize = 36f
        setOnClickListener {
            context.startActivity(Intent(context, activity))
        }
    }
}

inline fun TextField(
    context: Context,
    hint: String? = null,
    textSize: Float? = null,
    textColor: Int? = null,
    crossinline onTextChanged: (CharSequence?) -> Unit,
): TextView {
    return EditText(context).apply {
        if (hint != null) this.hint = hint
        if (textSize != null) this.textSize = textSize
        if (textColor != null) this.setTextColor(textColor)
        doOnTextChanged { text, _, _, _ ->
            onTextChanged(text)
        }
    }
}

inline fun TextField(
    context: Context,
    hint: String? = null,
    textSize: Float? = null,
    textColor: Int? = null,
): TextView {
    return EditText(context).apply {
        if (hint != null) this.hint = hint
        if (textSize != null) this.textSize = textSize
        if (textColor != null) this.setTextColor(textColor)
    }
}

inline fun Text(
    context: Context,
    text: CharSequence? = null,
    textSize: Float? = null,
    textColor: Int? = null,
): TextView = TextView(context).apply {
    if (text != null) this.text = text
    if (textSize != null) this.textSize = textSize
    if (textColor != null) this.setTextColor(textColor)
}

inline fun Button(
    context: Context,
    text: CharSequence? = null,
    textSize: Float? = null,
    textColor: Int? = null,
    noinline onClick: (View?) -> Unit,
): View = android.widget.Button(context).apply {
    if (text != null) this.text = text
    if (textSize != null) this.textSize = textSize
    if (textColor != null) this.setTextColor(textColor)
    setOnClickListener(onClick)
}

inline fun Column(context: Context, vararg views: View): View = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    views.forEach { addView(it) }
}

inline fun Row(context: Context, vararg views: View): View = LinearLayout(context).apply {
    orientation = LinearLayout.HORIZONTAL
    views.forEach { addView(it) }
}

inline fun Scrollable(context: Context, child: View): View = NestedScrollView(context).apply {
    addView(child)
}