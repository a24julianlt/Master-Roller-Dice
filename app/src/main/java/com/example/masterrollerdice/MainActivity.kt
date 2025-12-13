package com.example.masterrollerdice

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.masterrollerdice.databinding.ActivityMainBinding

/**
 * Activity principal que contiene la lógica de navegación y configuración global.
 * Gestiona el menú lateral (Drawer), la barra de herramientas (Toolbar) y la configuración de temas/sonido.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val model: DadosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.inicioFragment, R.id.historialFragment, R.id.estadisticasFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        setupDrawerSwitches()
    }

    /**
     * Configura los interruptores (switches) del menú lateral para Modo Oscuro, Sonido y Vibración.
     */
    private fun setupDrawerSwitches() {
        val menu = binding.navView.menu

        val thumbColor = ContextCompat.getColor(this, R.color.rojoPrincipal)
        val trackColor = ContextCompat.getColor(this, R.color.rojoSecundario)
        val thumbStateList = ColorStateList.valueOf(thumbColor)
        val trackStateList = ColorStateList.valueOf(trackColor)

        fun styleSwitch(switch: SwitchCompat) {
            switch.thumbTintList = thumbStateList
            switch.trackTintList = trackStateList
        }

        // --- 1. MODO OSCURO ---
        val themeItem = menu.findItem(R.id.theme_switch_item)
        val themeSwitch = themeItem.actionView as SwitchCompat
        styleSwitch(themeSwitch)

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isSystemNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()

        themeSwitch.isChecked = when (currentNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemNightMode
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        themeItem.setOnMenuItemClickListener {
            themeSwitch.isChecked = !themeSwitch.isChecked
            true
        }

        // --- 2. SONIDO ---
        val soundItem = menu.findItem(R.id.sound_switch_item)
        val soundSwitch = soundItem.actionView as SwitchCompat
        styleSwitch(soundSwitch)
        soundSwitch.isChecked = model.isSoundEnabled

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.setSoundPreference(isChecked)
        }
        soundItem.setOnMenuItemClickListener {
            soundSwitch.isChecked = !soundSwitch.isChecked
            true
        }

        // --- 3. VIBRACIÓN ---
        val vibItem = menu.findItem(R.id.vibration_switch_item)
        val vibSwitch = vibItem.actionView as SwitchCompat
        styleSwitch(vibSwitch)
        vibSwitch.isChecked = model.isVibrationEnabled

        vibSwitch.setOnCheckedChangeListener { _, isChecked ->
            model.setVibrationPreference(isChecked)
        }
        vibItem.setOnMenuItemClickListener {
            vibSwitch.isChecked = !vibSwitch.isChecked
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return NavigationUI.onNavDestinationSelected(
            item,
            navController
        ) || super.onOptionsItemSelected(item)
    }
}
