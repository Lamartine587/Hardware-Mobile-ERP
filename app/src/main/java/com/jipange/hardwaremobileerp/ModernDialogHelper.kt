package com.jipange.hardwaremobileerp

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

object ModernDialogHelper {

    fun createHeaderView(context: Context, iconEmoji: String, title: String, subtitle: String): View {
        val headerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 40, 48, 20)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvIcon = TextView(context).apply {
            text = iconEmoji
            textSize = 26f
            setPadding(0, 0, 16, 0)
        }

        val textStack = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = TextView(context).apply {
            text = title
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(context.getColor(R.color.text_primary))
        }

        val tvSub = TextView(context).apply {
            text = subtitle
            textSize = 12f
            setTextColor(context.getColor(R.color.text_secondary))
        }

        textStack.addView(tvTitle)
        textStack.addView(tvSub)

        topRow.addView(tvIcon)
        topRow.addView(textStack)

        val divider = View(context).apply {
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2)
            params.setMargins(0, 20, 0, 0)
            layoutParams = params
            setBackgroundColor(context.getColor(R.color.card_stroke))
        }

        headerLayout.addView(topRow)
        headerLayout.addView(divider)
        return headerLayout
    }

    fun createStyledInputField(
        context: Context,
        labelText: String,
        hintText: String,
        initialText: String = "",
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        enabled: Boolean = true
    ): Pair<LinearLayout, EditText> {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        val label = TextView(context).apply {
            text = labelText
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(context.getColor(R.color.text_secondary))
            setPadding(4, 0, 0, 8)
        }

        val editText = EditText(context).apply {
            hint = hintText
            setText(initialText)
            this.inputType = inputType
            isEnabled = enabled
            textSize = 14f
            setPadding(32, 28, 32, 28)
            setBackgroundResource(R.drawable.bg_search_input)
            setTextColor(context.getColor(R.color.input_text))
            setHintTextColor(context.getColor(R.color.input_hint))
        }

        container.addView(label)
        container.addView(editText)
        return Pair(container, editText)
    }

    fun createStyledSpinnerField(
        context: Context,
        labelText: String,
        options: List<String>,
        selectedIndex: Int = 0
    ): Pair<LinearLayout, Spinner> {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
        }

        val label = TextView(context).apply {
            text = labelText
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(context.getColor(R.color.text_secondary))
            setPadding(4, 0, 0, 8)
        }

        val spinnerContainer = FrameLayout(context).apply {
            setBackgroundResource(R.drawable.bg_search_input)
            setPadding(16, 8, 16, 8)
        }

        val spinner = Spinner(context).apply {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
            if (selectedIndex in options.indices) setSelection(selectedIndex)
        }

        spinnerContainer.addView(spinner)
        container.addView(label)
        container.addView(spinnerContainer)
        return Pair(container, spinner)
    }

    fun createFormScrollView(context: Context, contentLayout: LinearLayout): ScrollView {
        return ScrollView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            contentLayout.setPadding(48, 16, 48, 24)
            addView(contentLayout)
        }
    }
}
