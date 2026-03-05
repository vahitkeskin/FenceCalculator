package com.vahitkeskin.fencecalculator.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val COMPANY_NAME_KEY = stringPreferencesKey("company_name")
        val THEME_KEY = stringPreferencesKey("app_theme")
        val POLE_LENGTH_KEY = stringPreferencesKey("pole_length")
        val PIPE_LENGTH_KEY = stringPreferencesKey("pipe_length")
        val TENSION_FACTOR_KEY = stringPreferencesKey("tension_factor")
        val BINDING_FACTOR_KEY = stringPreferencesKey("binding_factor")
        val CEMENT_FACTOR_KEY = stringPreferencesKey("cement_factor")
        val CONCRETE_FACTOR_KEY = stringPreferencesKey("concrete_factor")
        val CUSTOM_CARDS_KEY = stringPreferencesKey("custom_cards")
        val HIDDEN_CARDS_KEY = stringPreferencesKey("hidden_cards")
        val CARD_ORDER_KEY = stringPreferencesKey("card_order")
    }

    val companyName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COMPANY_NAME_KEY] ?: ""
    }

    val theme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "SYSTEM"
    }

    val poleLength: Flow<String> = context.dataStore.data.map { it[POLE_LENGTH_KEY] ?: "2.4" }
    val pipeLength: Flow<String> = context.dataStore.data.map { it[PIPE_LENGTH_KEY] ?: "6.0" }
    val tensionFactor: Flow<String> = context.dataStore.data.map { it[TENSION_FACTOR_KEY] ?: "6.66" }
    val bindingFactor: Flow<String> = context.dataStore.data.map { it[BINDING_FACTOR_KEY] ?: "3.0" }
    val cementFactor: Flow<String> = context.dataStore.data.map { it[CEMENT_FACTOR_KEY] ?: "6.0" }
    val concreteFactor: Flow<String> = context.dataStore.data.map { it[CONCRETE_FACTOR_KEY] ?: "30.0" }

    val customCards: Flow<String> = context.dataStore.data.map { it[CUSTOM_CARDS_KEY] ?: "[]" }
    val hiddenCards: Flow<String> = context.dataStore.data.map { it[HIDDEN_CARDS_KEY] ?: "" }
    val cardOrder: Flow<String> = context.dataStore.data.map { it[CARD_ORDER_KEY] ?: "" }

    suspend fun saveCompanyName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[COMPANY_NAME_KEY] = name
        }
    }

    suspend fun saveTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun saveCustomCards(json: String) {
        context.dataStore.edit { it[CUSTOM_CARDS_KEY] = json }
    }

    suspend fun saveHiddenCards(csv: String) {
        context.dataStore.edit { it[HIDDEN_CARDS_KEY] = csv }
    }

    suspend fun saveCardOrder(csv: String) {
        context.dataStore.edit { it[CARD_ORDER_KEY] = csv }
    }

    suspend fun savePoleLength(v: String) = context.dataStore.edit { it[POLE_LENGTH_KEY] = v }
    suspend fun savePipeLength(v: String) = context.dataStore.edit { it[PIPE_LENGTH_KEY] = v }
    suspend fun saveTensionFactor(v: String) = context.dataStore.edit { it[TENSION_FACTOR_KEY] = v }
    suspend fun saveBindingFactor(v: String) = context.dataStore.edit { it[BINDING_FACTOR_KEY] = v }
    suspend fun saveCementFactor(v: String) = context.dataStore.edit { it[CEMENT_FACTOR_KEY] = v }
    suspend fun saveConcreteFactor(v: String) = context.dataStore.edit { it[CONCRETE_FACTOR_KEY] = v }
}
