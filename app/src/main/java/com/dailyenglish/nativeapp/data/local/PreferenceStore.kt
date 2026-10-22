package com.dailyenglish.nativeapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferenceDataStore by preferencesDataStore(name = "preferences")

interface PreferenceStore {
  val accent: Flow<String>
  suspend fun setAccent(value: String)
}

@Singleton
class DataStorePreferenceStore @Inject constructor(
  @ApplicationContext private val context: Context,
) : PreferenceStore {
  private val accentKey = stringPreferencesKey("accent")

  override val accent: Flow<String> = context.preferenceDataStore.data.map { it[accentKey] ?: "blue" }

  override suspend fun setAccent(value: String) {
    context.preferenceDataStore.edit { it[accentKey] = value }
  }
}
