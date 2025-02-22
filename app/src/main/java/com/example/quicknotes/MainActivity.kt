package com.example.quicknotes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var currentPathTextView: TextView
    private lateinit var backButton: ImageButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        val language = getLocalePreferences()
        setLocale(this, language)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        window.statusBarColor = resources.getColor(R.color.primary)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        menuButton = findViewById(R.id.menuButton)
        currentPathTextView = findViewById(R.id.parentFolderTextView)
        backButton = findViewById(R.id.backButton)
        menuButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val view = currentFocus
                view?.let {
                    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
                }
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is NoteDetailFragment) {
                        fragment.contentRichEditor.clearFocus()
                    }
                }
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        backButton.setOnClickListener {
            onBackPressed()
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_language -> showLanguageChangeDialog()
                R.id.menu_about -> showAboutAuthorsDialog()
            }
            true
        }

        val darkThemeSwitch = navView.menu.findItem(R.id.menu_dark_theme).actionView as SwitchCompat
        val isDarkThemeEnabled = getThemePreferences()
        darkThemeSwitch.isChecked = isDarkThemeEnabled
        if (isDarkThemeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        darkThemeSwitch.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            saveThemePreference(isChecked)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commit()
        }
    }

    private fun showAboutAuthorsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_about, null)

        val emailTextView: TextView = dialogView.findViewById(R.id.email_text_view)

        emailTextView.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:dev.support@yandex.ru")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback on QuickNotes")
            }
            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(emailIntent)
            }
        }

        MaterialAlertDialogBuilder(this).setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ -> }
            .create().show()
    }


    private fun showLanguageChangeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_language, null)
        val radioGroup: RadioGroup = dialogView.findViewById(R.id.radioGroup)

        MaterialAlertDialogBuilder(this).setView(dialogView)
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                val language = when (radioGroup.checkedRadioButtonId) {
                    R.id.englishRadioButton -> "en"
                    R.id.russianRadioButton -> "ru"
                    else -> "en"
                }
                setLocale(this, language)
                saveLocalePreferences(language)
            }.setNegativeButton(getString(R.string.cancel), null).create().show()
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        val sharedPreference = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        sharedPreference.edit().putBoolean("dark_theme", isDarkTheme).apply()
    }

    private fun saveLocalePreferences(language: String) {
        val sharedPreference = getSharedPreferences("locale_prefs", MODE_PRIVATE)
        sharedPreference.edit().putString("language", language).apply()
    }

    private fun getThemePreferences(): Boolean {
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        return sharedPreferences.getBoolean("dark_theme", false)
    }

    fun getLocalePreferences(): String? {
        val sharedPreferences = getSharedPreferences("locale_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("language", "en")
    }

    fun setLocale(context: Context, language: String?) {
        val newLocale = Locale(language)
        Locale.setDefault(newLocale)

        val config = context.resources.configuration
        config.setLocale(newLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        if (context is AppCompatActivity) {
            val currentLanguage = getLocalePreferences()
            if (currentLanguage != language) {
                context.recreate()
            }
        }
    }

    fun updateCurrentPathTextView(folderName: String?) {
        currentPathTextView.text = folderName ?: getString(R.string.root_folder)
    }
}