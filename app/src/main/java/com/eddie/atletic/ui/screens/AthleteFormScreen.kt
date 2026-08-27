package com.eddie.atletic.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eddie.atletic.data.model.Athlete
import com.eddie.atletic.data.model.Belt
import com.eddie.atletic.data.model.RenewalCalculator
import com.eddie.atletic.ui.AthletesViewModel
import com.eddie.atletic.ui.components.BeltPickerItem
import com.eddie.atletic.ui.components.RenewalStatusBadge
import com.eddie.atletic.ui.components.VisualBelt
import com.eddie.atletic.ui.theme.BluePrimary
import com.eddie.atletic.ui.theme.GoldAccent
import com.eddie.atletic.ui.theme.NavyCard
import com.eddie.atletic.ui.theme.NavyCardElevated
import java.util.Calendar

private val weightCategories = listOf(
    "Galo", "Pluma", "Pena", "Leve", "Médio",
    "Meio-Pesado", "Pesado", "Super-Pesado", "Pesadíssimo", "Absoluto"
)

private val modalities = listOf(
    "Jiu-Jitsu", "Judô", "Karatê", "Taekwondo", "Muay Thai", "Boxe", "MMA"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthleteFormScreen(
    athleteId: String?,
    viewModel: AthletesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val existingAthlete = remember(athleteId, uiState.athletes) {
        if (!athleteId.isNullOrBlank()) viewModel.getAthleteById(athleteId) else null
    }

    val isEditing = existingAthlete != null

    // Form fields state
    var name by remember(existingAthlete) { mutableStateOf(existingAthlete?.name ?: "") }
    var registrationNumber by remember(existingAthlete) {
        mutableStateOf(existingAthlete?.registrationNumber ?: "")
    }
    var cpf by remember(existingAthlete) { mutableStateOf(existingAthlete?.cpf ?: "") }
    var birthDate by remember(existingAthlete) { mutableStateOf(existingAthlete?.birthDate ?: "1998-05-15") }
    var academy by remember(existingAthlete) { mutableStateOf(existingAthlete?.academy ?: "") }
    var modality by remember(existingAthlete) { mutableStateOf(existingAthlete?.modality ?: "Jiu-Jitsu") }
    var selectedBelt by remember(existingAthlete) {
        mutableStateOf(existingAthlete?.belt ?: Belt.WHITE)
    }
    var degrees by remember(existingAthlete) { mutableIntStateOf(existingAthlete?.degrees ?: 0) }
    var weightCategory by remember(existingAthlete) { mutableStateOf(existingAthlete?.weightCategory ?: "Médio") }
    var weightKg by remember(existingAthlete) { mutableDoubleStateOf(existingAthlete?.weightKg ?: 82.0) }
    var gender by remember(existingAthlete) { mutableStateOf(existingAthlete?.gender ?: "Masculino") }
    var registrationDate by remember(existingAthlete) {
        mutableStateOf(existingAthlete?.registrationDate ?: RenewalCalculator.todayIso())
    }
    var renewalDate by remember(existingAthlete) {
        mutableStateOf(existingAthlete?.renewalDate ?: RenewalCalculator.oneYearFromTodayIso())
    }
    var phone by remember(existingAthlete) { mutableStateOf(existingAthlete?.phone ?: "") }
    var email by remember(existingAthlete) { mutableStateOf(existingAthlete?.email ?: "") }
    var notes by remember(existingAthlete) { mutableStateOf(existingAthlete?.notes ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var academyError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // DatePicker helper for Renewal Date
    val openRenewalDatePicker = {
        val cal = Calendar.getInstance()
        RenewalCalculator.parseDate(renewalDate)?.let { cal.time = it }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val monthStr = String.format("%02d", month + 1)
                val dayStr = String.format("%02d", dayOfMonth)
                renewalDate = "$year-$monthStr-$dayStr"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // DatePicker helper for Birth Date
    val openBirthDatePicker = {
        val cal = Calendar.getInstance()
        RenewalCalculator.parseDate(birthDate)?.let { cal.time = it }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val monthStr = String.format("%02d", month + 1)
                val dayStr = String.format("%02d", dayOfMonth)
                birthDate = "$year-$monthStr-$dayStr"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Atleta" else "Novo Cadastro de Atleta",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyCard
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // LIVE PREVIEW CARD OF MARTIAL BELT & GRADUATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyCardElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRÉ-VISUALIZAÇÃO DA GRADUAÇÃO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    VisualBelt(
                        belt = selectedBelt,
                        degrees = degrees,
                        height = 32.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = selectedBelt.getFullGraduationTitle(degrees),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val previewRenewalInfo = RenewalCalculator.calculate(renewalDate)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Status Anual: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        RenewalStatusBadge(
                            renewalInfo = previewRenewalInfo,
                            compact = true
                        )
                    }
                }
            }

            // SECTION: DADOS PESSOAIS
            FormSectionHeader(title = "Dados Pessoais", icon = Icons.Default.Person)

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = if (it.isBlank()) "Nome completo é obrigatório" else null
                },
                label = { Text("Nome Completo do Atleta *") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cpf,
                    onValueChange = { cpf = it },
                    label = { Text("CPF / Doc") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f)
                )

                OutlinedTextField(
                    value = RenewalCalculator.formatDateToDisplay(birthDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nascimento") },
                    trailingIcon = {
                        IconButton(onClick = openBirthDatePicker) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Selecionar Data",
                                tint = GoldAccent
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultFieldColors(),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { openBirthDatePicker() }
                )
            }

            // Gênero
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Gênero:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCBD5E1))
                listOf("Masculino", "Feminino").forEach { item ->
                    FilterChip(
                        selected = gender == item,
                        onClick = { gender = item },
                        label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = NavyCard,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // SECTION: DADOS FEDERATIVOS & ACADEMIA
            FormSectionHeader(title = "Dados Federativos & Equipe", icon = Icons.Default.LocationOn)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it },
                    label = { Text("Matrícula da Federação") },
                    placeholder = { Text("Ex: FED-2026-1001") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultFieldColors(),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        registrationNumber = "FED-${Calendar.getInstance().get(Calendar.YEAR)}-${(1000..9999).random()}"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Gerar",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gerar", color = GoldAccent, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = academy,
                onValueChange = {
                    academy = it
                    academyError = if (it.isBlank()) "Nome da academia/CT é obrigatório" else null
                },
                label = { Text("Academia / Equipe / CT *") },
                placeholder = { Text("Ex: Alliance, Gracie Barra, Nova União...") },
                isError = academyError != null,
                supportingText = academyError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Modalidade
            Text(
                text = "Modalidade Esportiva:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )
            val modalityScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(modalityScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modalities.forEach { item ->
                    FilterChip(
                        selected = modality == item,
                        onClick = { modality = item },
                        label = { Text(item) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = NavyCard,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // SECTION: GRADUAÇÃO (FAIXA E GRAUS)
            FormSectionHeader(title = "Graduação: Faixa & Graus", icon = Icons.Default.MilitaryTech)

            Text(
                text = "Selecione a Faixa Atual:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )

            val beltScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(beltScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Belt.values().forEach { belt ->
                    BeltPickerItem(
                        belt = belt,
                        isSelected = selectedBelt == belt,
                        onClick = {
                            selectedBelt = belt
                            if (degrees > belt.maxDegrees) {
                                degrees = belt.maxDegrees
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Graus na Faixa (0 a ${selectedBelt.maxDegrees}):",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (0..selectedBelt.maxDegrees.coerceAtMost(4)).forEach { degreeNumber ->
                    FilterChip(
                        selected = degrees == degreeNumber,
                        onClick = { degrees = degreeNumber },
                        label = {
                            Text(
                                text = if (degreeNumber == 0) "Sem Grau (0)" else "$degreeNumber º Grau",
                                fontWeight = if (degrees == degreeNumber) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = Color.Black,
                            containerColor = NavyCard,
                            labelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SECTION: CATEGORIA DE PESO
            FormSectionHeader(title = "Categoria de Peso & Balança", icon = Icons.Default.FitnessCenter)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightKg.toString(),
                    onValueChange = {
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null) weightKg = parsed
                    },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultFieldColors(),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = weightCategory,
                    onValueChange = { weightCategory = it },
                    label = { Text("Categoria") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = defaultFieldColors(),
                    modifier = Modifier.weight(1.4f)
                )
            }

            val catScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(catScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                weightCategories.forEach { cat ->
                    FilterChip(
                        selected = weightCategory == cat,
                        onClick = { weightCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = NavyCard,
                            labelColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // SECTION: MATRÍCULA & RENOVAÇÃO ANUAL
            FormSectionHeader(title = "Controle de Anuidade & Renovação", icon = Icons.Default.CalendarMonth)

            OutlinedTextField(
                value = RenewalCalculator.formatDateToDisplay(renewalDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data de Renovação Anual de Matrícula *") },
                supportingText = {
                    Text(
                        text = "Vencimento da anuidade federativa (calcula status automaticamente)",
                        color = Color(0xFF94A3B8)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = openRenewalDatePicker) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Selecionar Data",
                            tint = GoldAccent
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openRenewalDatePicker() }
            )

            // Quick Date Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { renewalDate = RenewalCalculator.todayIso() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hoje", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { renewalDate = RenewalCalculator.oneYearFromTodayIso() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+1 Ano", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }
                        renewalDate = RenewalCalculator.formatDateToIso(cal.time)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simular Vencida", fontSize = 10.sp)
                }
            }

            // SECTION: CONTATO & OBSERVAÇÕES
            FormSectionHeader(title = "Contato & Histórico", icon = Icons.Default.Phone)

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone / WhatsApp") },
                placeholder = { Text("(11) 98765-4321") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                placeholder = { Text("atleta@exemplo.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações / Títulos / Histórico") },
                placeholder = { Text("Ex: Campeão Pan-Americano 2024...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = defaultFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "Nome completo é obrigatório"
                        return@Button
                    }
                    if (academy.isBlank()) {
                        academyError = "Nome da academia é obrigatório"
                        return@Button
                    }

                    val athleteToSave = Athlete(
                        id = existingAthlete?.id ?: "",
                        name = name.trim(),
                        registrationNumber = registrationNumber.trim().ifBlank {
                            viewModel.getAthleteById(existingAthlete?.id ?: "")?.registrationNumber ?: ""
                        },
                        cpf = cpf.trim(),
                        birthDate = birthDate.trim(),
                        academy = academy.trim(),
                        modality = modality.trim(),
                        beltName = selectedBelt.name,
                        degrees = degrees,
                        weightCategory = weightCategory.trim(),
                        weightKg = weightKg,
                        gender = gender,
                        registrationDate = registrationDate.trim(),
                        renewalDate = renewalDate.trim(),
                        phone = phone.trim(),
                        email = email.trim(),
                        notes = notes.trim(),
                        avatarColorIndex = existingAthlete?.avatarColorIndex ?: (0..5).random(),
                        createdAt = existingAthlete?.createdAt ?: 0L,
                        updatedAt = System.currentTimeMillis()
                    )

                    viewModel.saveAthlete(athleteToSave) { success ->
                        if (success) {
                            onNavigateBack()
                        }
                    }
                },
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) "Salvar Alterações" else "Cadastrar Atleta na Federação",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FormSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun defaultFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NavyCard,
    unfocusedContainerColor = NavyCard,
    focusedBorderColor = BluePrimary,
    unfocusedBorderColor = Color(0xFF334155),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = BluePrimary,
    unfocusedLabelColor = Color(0xFF94A3B8)
)

