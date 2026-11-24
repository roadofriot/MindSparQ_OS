package com.mindsparq.os.features.academic

import com.mindsparq.os.core.network.SupabaseConfig
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: String,
    @SerialName("course_code") val courseCode: String? = null,
    val name: String,
    val description: String? = null,
    @SerialName("age_min") val ageMin: Int? = null,
    @SerialName("age_max") val ageMax: Int? = null,
    val status: String = "active",
)

@Serializable
data class CourseCreateRequest(
    @SerialName("course_code") val courseCode: String? = null,
    val name: String,
    val description: String? = null,
)

@Serializable
data class ProgramDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    @SerialName("course_id") val courseId: String,
    val name: String,
    @SerialName("program_type") val programType: String = "custom",
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    val status: String = "planned",
)

@Serializable
data class TeacherDto(
    val id: String,
    @SerialName("teacher_code") val teacherCode: String? = null,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    val designation: String? = null,
    val status: String = "active",
)

class AcademicRepository {
    suspend fun listCourses(): Result<List<CourseDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("courses")
            .select { order("name", Order.ASCENDING) }
            .decodeList<CourseDto>()
    }

    suspend fun createCourse(request: CourseCreateRequest): Result<CourseDto> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        require(request.name.isNotBlank()) { "Course name is required." }
        client.from("courses")
            .insert(request) { select() }
            .decodeSingle<CourseDto>()
    }

    suspend fun listPrograms(): Result<List<ProgramDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("programs")
            .select { order("start_date", Order.DESCENDING) }
            .decodeList<ProgramDto>()
    }

    suspend fun listTeachers(): Result<List<TeacherDto>> = runCatching {
        val client = SupabaseConfig.client ?: error("Supabase is not configured.")
        client.from("teachers")
            .select { order("full_name", Order.ASCENDING) }
            .decodeList<TeacherDto>()
    }
}
