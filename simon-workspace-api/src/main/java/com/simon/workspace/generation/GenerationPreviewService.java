package com.simon.workspace.generation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.workspace.auth.session.AuthContextHolder;
import com.simon.workspace.generation.dto.GenerationTaskResponse;
import com.simon.workspace.generation.dto.LessonPreviewRequest;
import com.simon.workspace.generation.dto.LessonPreviewResponse;
import com.simon.workspace.generation.model.GenerationTaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class GenerationPreviewService {

    private static final String TASK_TYPE_LESSON = "TEACHING_PLAN";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GenerationPreviewService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public LessonPreviewResponse createLessonPreview(LessonPreviewRequest request) {
        long ownerUserId = AuthContextHolder.requireUser().id();
        CourseSnapshot course = findCourse(request.courseId());
        ClassSnapshot classInfo = findClassInfo(request.classId());
        SemesterSnapshot semester = findSemester(request.semesterId());
        TemplateSnapshot template = findWordTemplate(request.templateId());
        WeekSnapshot week = findWeek(request.semesterId(), request.weekNo(), semester);
        List<TemplateFieldSnapshot> fields = findTemplateFields(request.templateId());

        String topic = normalizeTopic(request.topic(), course, request.weekNo());
        String inputJson = writeJson(buildInputSnapshot(request, topic));
        String dataJson = writeJson(buildLessonData(course, classInfo, semester, week, template, fields, topic));
        String taskName = topic + " - 教案";

        long taskId = createTask(ownerUserId, request, taskName, inputJson);
        long documentDataId = createDocumentData(taskId, dataJson);

        return new LessonPreviewResponse(
                GenerationTaskResponse.from(findTask(taskId, ownerUserId)),
                String.valueOf(documentDataId),
                dataJson
        );
    }

    private long createTask(long ownerUserId, LessonPreviewRequest request, String taskName, String inputJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO generate_task (
                                owner_user_id, task_type, task_name, course_id, class_id,
                                semester_id, template_id, status, input_json, result_summary
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, ownerUserId);
            statement.setString(2, TASK_TYPE_LESSON);
            statement.setString(3, taskName);
            statement.setLong(4, request.courseId());
            statement.setLong(5, request.classId());
            statement.setLong(6, request.semesterId());
            statement.setLong(7, request.templateId());
            statement.setString(8, GenerationTaskStatus.PREVIEW_READY.name());
            statement.setString(9, inputJson);
            statement.setString(10, "教案预览已生成");
            return statement;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private long createDocumentData(long taskId, String dataJson) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO document_data (task_id, document_type, data_json)
                            VALUES (?, ?, ?)
                            """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setLong(1, taskId);
            statement.setString(2, TASK_TYPE_LESSON);
            statement.setString(3, dataJson);
            return statement;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Map<String, Object> buildInputSnapshot(LessonPreviewRequest request, String topic) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("courseId", request.courseId());
        input.put("classId", request.classId());
        input.put("semesterId", request.semesterId());
        input.put("templateId", request.templateId());
        input.put("weekNo", request.weekNo());
        input.put("topic", topic);
        return input;
    }

    private Map<String, Object> buildLessonData(
            CourseSnapshot course,
            ClassSnapshot classInfo,
            SemesterSnapshot semester,
            WeekSnapshot week,
            TemplateSnapshot template,
            List<TemplateFieldSnapshot> fields,
            String topic
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("documentType", TASK_TYPE_LESSON);
        data.put("topic", topic);
        data.put("course", course.toMap());
        data.put("classInfo", classInfo.toMap());
        data.put("semester", semester.toMap());
        data.put("week", week.toMap());
        data.put("template", template.toMap(fields));
        data.put("fieldValues", buildFieldValues(fields, course, classInfo, semester, week, topic));
        data.put("lesson", buildLessonBody(course, classInfo, week, topic));
        return data;
    }

    private Map<String, Object> buildLessonBody(
            CourseSnapshot course,
            ClassSnapshot classInfo,
            WeekSnapshot week,
            String topic
    ) {
        Map<String, Object> lesson = new LinkedHashMap<>();
        lesson.put("title", topic);
        lesson.put("className", classInfo.className());
        lesson.put("weekNo", week.weekNo());
        lesson.put("dateRange", week.startDate() + " - " + week.endDate());
        lesson.put("teachingObjectives", blankToEmpty(course.courseGoal()));
        lesson.put("teachingKeyPoints", blankToEmpty(course.keyPoint()));
        lesson.put("teachingDifficultPoints", blankToEmpty(course.difficultPoint()));
        lesson.put("teachingMethods", blankToEmpty(course.assessmentMethod()));
        lesson.put("teachingProcess", defaultProcess());
        lesson.put("homework", "");
        lesson.put("ideologicalElement", "");
        lesson.put("reflection", "");
        return lesson;
    }

    private List<Map<String, Object>> defaultProcess() {
        List<Map<String, Object>> process = new ArrayList<>();
        process.add(processStep("导入", ""));
        process.add(processStep("新课讲授", ""));
        process.add(processStep("课堂练习", ""));
        process.add(processStep("小结", ""));
        return process;
    }

    private Map<String, Object> processStep(String name, String content) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("name", name);
        step.put("content", content);
        return step;
    }

    private Map<String, Object> buildFieldValues(
            List<TemplateFieldSnapshot> fields,
            CourseSnapshot course,
            ClassSnapshot classInfo,
            SemesterSnapshot semester,
            WeekSnapshot week,
            String topic
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (TemplateFieldSnapshot field : fields) {
            values.put(field.fieldKey(), StringUtils.hasText(field.defaultValue())
                    ? field.defaultValue().trim()
                    : inferFieldValue(field.fieldKey(), course, classInfo, semester, week, topic));
        }
        return values;
    }

    private Object inferFieldValue(
            String key,
            CourseSnapshot course,
            ClassSnapshot classInfo,
            SemesterSnapshot semester,
            WeekSnapshot week,
            String topic
    ) {
        return switch (key) {
            case "courseName" -> course.courseName();
            case "courseCode" -> course.courseCode();
            case "className" -> classInfo.className();
            case "major" -> firstText(classInfo.major(), course.major());
            case "grade" -> firstText(classInfo.grade(), course.grade());
            case "semesterName" -> semester.academicYear() + " " + semester.semesterName();
            case "weekNo" -> week.weekNo();
            case "dateRange" -> week.startDate() + " - " + week.endDate();
            case "topic", "title" -> topic;
            case "teachingObjectives" -> blankToEmpty(course.courseGoal());
            case "teachingKeyPoints", "keyPoint" -> blankToEmpty(course.keyPoint());
            case "teachingDifficultPoints", "difficultPoint" -> blankToEmpty(course.difficultPoint());
            case "textbook" -> blankToEmpty(course.textbook());
            default -> "";
        };
    }

    private CourseSnapshot findCourse(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM course
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> new CourseSnapshot(
                        rs.getLong("id"),
                        rs.getString("course_name"),
                        rs.getString("course_code"),
                        rs.getString("major"),
                        rs.getString("grade"),
                        (Integer) rs.getObject("total_hours"),
                        (Integer) rs.getObject("theory_hours"),
                        (Integer) rs.getObject("experiment_hours"),
                        (Integer) rs.getObject("weekly_hours"),
                        rs.getBigDecimal("credit"),
                        rs.getString("textbook"),
                        rs.getString("course_goal"),
                        rs.getString("key_point"),
                        rs.getString("difficult_point"),
                        rs.getString("assessment_method")
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("课程不存在"));
    }

    private ClassSnapshot findClassInfo(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM class_info
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> new ClassSnapshot(
                        rs.getLong("id"),
                        rs.getString("class_name"),
                        rs.getString("major"),
                        rs.getString("grade"),
                        (Integer) rs.getObject("student_count")
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("班级不存在"));
    }

    private SemesterSnapshot findSemester(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM semester
                        WHERE id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> new SemesterSnapshot(
                        rs.getLong("id"),
                        rs.getString("academic_year"),
                        rs.getString("semester_name"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("end_date", LocalDate.class),
                        rs.getInt("total_weeks")
                ),
                id
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("学期不存在"));
    }

    private TemplateSnapshot findWordTemplate(long id) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM template_file
                        WHERE id = ? AND deleted = 0 AND status = 'ACTIVE'
                        LIMIT 1
                        """,
                (rs, rowNum) -> new TemplateSnapshot(
                        rs.getLong("id"),
                        rs.getString("template_name"),
                        rs.getString("template_type"),
                        rs.getString("original_filename")
                ),
                id
        ).stream().findFirst().map(template -> {
            if (!"WORD".equals(template.templateType())) {
                throw new IllegalArgumentException("教案生成需要选择 Word 模板");
            }
            return template;
        }).orElseThrow(() -> new IllegalArgumentException("模板不存在"));
    }

    private WeekSnapshot findWeek(long semesterId, int weekNo, SemesterSnapshot semester) {
        if (weekNo < 1 || weekNo > semester.totalWeeks()) {
            throw new IllegalArgumentException("周次必须在学期总周数范围内");
        }

        return jdbcTemplate.query("""
                        SELECT *
                        FROM semester_calendar
                        WHERE semester_id = ? AND week_no = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> new WeekSnapshot(
                        rs.getInt("week_no"),
                        rs.getObject("start_date", LocalDate.class),
                        rs.getObject("end_date", LocalDate.class),
                        rs.getBoolean("is_exam_week"),
                        rs.getBoolean("is_holiday"),
                        rs.getString("holiday_note"),
                        rs.getString("adjustment_note")
                ),
                semesterId,
                weekNo
        ).stream().findFirst().orElseGet(() -> calculatedWeek(semester, weekNo));
    }

    private WeekSnapshot calculatedWeek(SemesterSnapshot semester, int weekNo) {
        LocalDate startDate = semester.startDate().plusWeeks(weekNo - 1L);
        LocalDate endDate = startDate.plusDays(6);
        if (semester.endDate() != null && endDate.isAfter(semester.endDate())) {
            endDate = semester.endDate();
        }
        return new WeekSnapshot(weekNo, startDate, endDate, false, false, null, null);
    }

    private List<TemplateFieldSnapshot> findTemplateFields(long templateId) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM template_field
                        WHERE template_id = ? AND deleted = 0 AND status = 'ACTIVE'
                        ORDER BY sort_order ASC, id ASC
                        """,
                (rs, rowNum) -> new TemplateFieldSnapshot(
                        rs.getLong("id"),
                        rs.getString("field_key"),
                        rs.getString("field_label"),
                        rs.getString("field_type"),
                        rs.getBoolean("required"),
                        rs.getString("default_value"),
                        rs.getInt("sort_order"),
                        rs.getString("remark")
                ),
                templateId
        );
    }

    private com.simon.workspace.generation.model.GenerationTask findTask(long taskId, long ownerUserId) {
        return jdbcTemplate.query("""
                        SELECT *
                        FROM generate_task
                        WHERE id = ? AND owner_user_id = ? AND deleted = 0
                        LIMIT 1
                        """,
                (rs, rowNum) -> GenerationTaskRowMapper.map(rs),
                taskId,
                ownerUserId
        ).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("生成任务不存在或无权访问"));
    }

    private String normalizeTopic(String topic, CourseSnapshot course, int weekNo) {
        if (StringUtils.hasText(topic)) {
            return topic.trim();
        }
        return course.courseName() + "第" + weekNo + "周教案";
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("生成预览 JSON 失败", exception);
        }
    }

    private String firstText(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first.trim();
        }
        return blankToEmpty(second);
    }

    private String blankToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private record CourseSnapshot(
            long id,
            String courseName,
            String courseCode,
            String major,
            String grade,
            Integer totalHours,
            Integer theoryHours,
            Integer experimentHours,
            Integer weeklyHours,
            BigDecimal credit,
            String textbook,
            String courseGoal,
            String keyPoint,
            String difficultPoint,
            String assessmentMethod
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(id));
            map.put("courseName", courseName);
            map.put("courseCode", courseCode);
            map.put("major", major);
            map.put("grade", grade);
            map.put("totalHours", totalHours);
            map.put("theoryHours", theoryHours);
            map.put("experimentHours", experimentHours);
            map.put("weeklyHours", weeklyHours);
            map.put("credit", credit);
            map.put("textbook", textbook);
            map.put("courseGoal", courseGoal);
            map.put("keyPoint", keyPoint);
            map.put("difficultPoint", difficultPoint);
            map.put("assessmentMethod", assessmentMethod);
            return map;
        }
    }

    private record ClassSnapshot(
            long id,
            String className,
            String major,
            String grade,
            Integer studentCount
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(id));
            map.put("className", className);
            map.put("major", major);
            map.put("grade", grade);
            map.put("studentCount", studentCount);
            return map;
        }
    }

    private record SemesterSnapshot(
            long id,
            String academicYear,
            String semesterName,
            LocalDate startDate,
            LocalDate endDate,
            Integer totalWeeks
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(id));
            map.put("academicYear", academicYear);
            map.put("semesterName", semesterName);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            map.put("totalWeeks", totalWeeks);
            return map;
        }
    }

    private record WeekSnapshot(
            Integer weekNo,
            LocalDate startDate,
            LocalDate endDate,
            Boolean examWeek,
            Boolean holiday,
            String holidayNote,
            String adjustmentNote
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("weekNo", weekNo);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            map.put("examWeek", examWeek);
            map.put("holiday", holiday);
            map.put("holidayNote", holidayNote);
            map.put("adjustmentNote", adjustmentNote);
            return map;
        }
    }

    private record TemplateSnapshot(
            long id,
            String templateName,
            String templateType,
            String originalFilename
    ) {
        private Map<String, Object> toMap(List<TemplateFieldSnapshot> fields) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(id));
            map.put("templateName", templateName);
            map.put("templateType", templateType);
            map.put("originalFilename", originalFilename);
            map.put("fields", fields.stream().map(TemplateFieldSnapshot::toMap).toList());
            return map;
        }
    }

    private record TemplateFieldSnapshot(
            long id,
            String fieldKey,
            String fieldLabel,
            String fieldType,
            Boolean required,
            String defaultValue,
            Integer sortOrder,
            String remark
    ) {
        private Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", String.valueOf(id));
            map.put("fieldKey", fieldKey);
            map.put("fieldLabel", fieldLabel);
            map.put("fieldType", fieldType);
            map.put("required", required);
            map.put("defaultValue", defaultValue);
            map.put("sortOrder", sortOrder);
            map.put("remark", remark);
            return map;
        }
    }
}
