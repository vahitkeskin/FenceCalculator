package com.vahitkeskin.fencecalculator.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.data.model.Country
import com.vahitkeskin.fencecalculator.data.model.CountryData
import com.vahitkeskin.fencecalculator.util.PhoneVisualTransformation
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.util.centerOnFocus
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNumberField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    selectCountryLabel: String,
    searchCountryLabel: String,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val context = LocalContext.current
    val phoneUtil = remember { PhoneNumberUtil.createInstance(context) }
    
    var selectedCountry by remember { 
        mutableStateOf(CountryData.allCountries.find { it.code == "TR" } ?: CountryData.allCountries.first()) 
    }
    
    var showCountryPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    
    // Auto-detect country code from existing phone number if it starts with +
    LaunchedEffect(phoneNumber) {
        if (phoneNumber.startsWith("+")) {
            val potentialCountry = CountryData.allCountries
                .sortedByDescending { it.dialCode.length }
                .find { phoneNumber.startsWith(it.dialCode) }
            if (potentialCountry != null) {
                selectedCountry = potentialCountry
            }
        }
    }

    val visualTransformation = remember(selectedCountry.code) {
        PhoneVisualTransformation(selectedCountry.code, phoneUtil)
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = phoneNumber.removePrefix(selectedCountry.dialCode).trim(),
            onValueChange = { newValue ->
                // Sadece rakamları al
                val digits = newValue.filter { it.isDigit() }
                onPhoneNumberChange(selectedCountry.dialCode + digits)
            },
            label = { Text(label, color = onBackgroundColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Row(
                    modifier = Modifier
                        .clickable { showCountryPicker = true }
                        .padding(start = 12.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCountry.flagEmoji, fontSize = 20.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        selectedCountry.dialCode, 
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = onBackgroundColor
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Phone, 
                        null, 
                        tint = onBackgroundColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth().centerOnFocus(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor)
        )
    }

    if (showCountryPicker) {
        ModalBottomSheet(
            onDismissRequest = { showCountryPicker = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            CountryPickerDialogContent(
                onCountrySelected = {
                    selectedCountry = it
                    showCountryPicker = false
                },
                onBackgroundColor = onBackgroundColor,
                selectCountryLabel = selectCountryLabel,
                searchCountryLabel = searchCountryLabel
            )
        }
    }
}

@Composable
fun CountryPickerDialogContent(
    onCountrySelected: (Country) -> Unit,
    onBackgroundColor: Color,
    selectCountryLabel: String,
    searchCountryLabel: String
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            CountryData.allCountries
        } else {
            CountryData.allCountries.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.dialCode.contains(searchQuery) ||
                it.code.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            selectCountryLabel,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(searchCountryLabel, color = onBackgroundColor.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            modifier = Modifier.fillMaxWidth().centerOnFocus(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                imeAction = androidx.compose.ui.text.input.ImeAction.Search
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCountries) { country ->
                Surface(
                    onClick = { onCountrySelected(country) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(country.flagEmoji, fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            country.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            country.dialCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
