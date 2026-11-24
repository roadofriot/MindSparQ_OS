package com.mindsparq.os.features.academic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mindsparq.os.core.theme.LocalMindSparqColors
import com.mindsparq.os.core.ui.MindSparqIconButton
import com.mindsparq.os.core.ui.StateContainer
import com.mindsparq.os.core.ui.UiState
import kotlinx.coroutines.launch

private enum class AcademicTab { COURSES, PROGRAMS, TEACHERS }

@Composable
fun AcademicScreen() {
    val colors = LocalMindSparqColors.current
    val repo = remember { AcademicRepository() }
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(AcademicTab.COURSES) }
    var coursesState by remember { mutableStateOf<UiState<List<CourseDto>>>(UiState.Loading) }
    var programsState by remember { mutableStateOf<UiState<List<ProgramDto>>>(UiState.Loading) }
    var teachersState by remember { mutableStateOf<UiState<List<TeacherDto>>>(UiState.Loading) }
    var showCreateCourse by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            when (activeTab) {
                AcademicTab.COURSES -> {
                    coursesState = UiState.Loading
                    repo.listCourses()
                        .onSuccess { list ->
                            coursesState = if (list.isEmpty()) {
                                UiState.Empty("No Courses", "Create your first course in the catalogue using +.")
                            } else UiState.Success(list)
                        }
                        .onFailure { coursesState = UiState.Error(it.message ?: "Failed to load courses") { reload() } }
                }
                AcademicTab.PROGRAMS -> {
                    programsState = UiState.Loading
                    repo.listPrograms()
                        .onSuccess { list ->
                            programsState = if (list.isEmpty()) {
                                UiState.Empty("No Programs", "No school delivery programs scheduled yet.")
                            } else UiState.Success(list)
                        }
                        .onFailure { programsState = UiState.Error(it.message ?: "Failed to load programs") { reload() } }
                }
                AcademicTab.TEACHERS -> {
                    teachersState = UiState.Loading
                    repo.listTeachers()
                        .onSuccess { list ->
                            teachersState = if (list.isEmpty()) {
                                UiState.Empty("No Instructors", "No instructor profiles found.")
                            } else UiState.Success(list)
                        }
                        .onFailure { teachersState = UiState.Error(it.message ?: "Failed to load instructors") { reload() } }
                }
            }
        }
    }

    LaunchedEffect(activeTab) { reload() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.MenuBook,
                    tooltip = "Course Catalogue",
                    selected = activeTab == AcademicTab.COURSES,
                    onClick = { activeTab = AcademicTab.COURSES },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.EventNote,
                    tooltip = "School Programs",
                    selected = activeTab == AcademicTab.PROGRAMS,
                    onClick = { activeTab = AcademicTab.PROGRAMS },
                )
                MindSparqIconButton(
                    icon = Icons.Outlined.People,
                    tooltip = "Instructors",
                    selected = activeTab == AcademicTab.TEACHERS,
                    onClick = { activeTab = AcademicTab.TEACHERS },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MindSparqIconButton(
                    icon = Icons.Outlined.Refresh,
                    tooltip = "Refresh",
                    outlined = true,
                    onClick = { reload() },
                )
                if (activeTab == AcademicTab.COURSES) {
                    MindSparqIconButton(
                        icon = Icons.Outlined.Add,
                        tooltip = "New Course",
                        outlined = true,
                        tint = colors.accentPrimary,
                        onClick = { showCreateCourse = true },
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderHairline)

        when (activeTab) {
            AcademicTab.COURSES -> StateContainer(state = coursesState) { courses ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(courses, key = { it.id }) { c ->
                        AcademicItemCard(
                            title = c.name,
                            subtitle = listOfNotNull(c.courseCode, c.description).joinToString(" • ").ifBlank { "No description" },
                            badge = c.status,
                        )
                    }
                }
            }
            AcademicTab.PROGRAMS -> StateContainer(state = programsState) { programs ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(programs, key = { it.id }) { p ->
                        AcademicItemCard(
                            title = p.name,
                            subtitle = "${p.startDate} → ${p.endDate ?: "Ongoing"}",
                            badge = p.status,
                        )
                    }
                }
            }
            AcademicTab.TEACHERS -> StateContainer(state = teachersState) { teachers ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                ) {
                    items(teachers, key = { it.id }) { t ->
                        AcademicItemCard(
                            title = t.fullName,
                            subtitle = listOfNotNull(t.teacherCode, t.designation, t.phone).joinToString(" • ").ifBlank { "No details" },
                            badge = t.status,
                        )
                    }
                }
            }
        }
    }

    if (showCreateCourse) {
        CreateCourseDialog(
            onDismiss = { showCreateCourse = false },
            onSubmit = { req, onErr ->
                scope.launch {
                    repo.createCourse(req)
                        .onSuccess {
                            showCreateCourse = false
                            reload()
                        }
                        .onFailure { onErr(it.message ?: "Failed to create course") }
                }
            },
        )
    }
}

@Composable
private fun AcademicItemCard(title: String, subtitle: String, badge: String) {
    val colors = LocalMindSparqColors.current
    Surface(
        color = colors.bgSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.borderHairline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentSecondary,
                modifier = Modifier
                    .background(colors.accentSecondary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CreateCourseDialog(
    onDismiss: () -> Unit,
    onSubmit: (CourseCreateRequest, onError: (String) -> Unit) -> Unit,
) {
    val colors = LocalMindSparqColors.current
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it; errorMsg = null }, label = { Text("Course Name *") }, singleLine = true)
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.labelMedium, color = colors.statusDanger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        CourseCreateRequest(
                            name = name.trim(),
                            courseCode = code.trim().ifBlank { null },
                            description = description.trim().ifBlank { null },
                        ),
                    ) { errorMsg = it }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
