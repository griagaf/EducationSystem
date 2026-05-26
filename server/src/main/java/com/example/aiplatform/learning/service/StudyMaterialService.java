package com.example.aiplatform.learning.service;

import com.example.aiplatform.common.exception.BadRequestException;
import com.example.aiplatform.common.exception.ResourceNotFoundException;
import com.example.aiplatform.learning.dto.StudyMaterialResponse;
import com.example.aiplatform.learning.entity.LearningGoal;
import com.example.aiplatform.learning.entity.StudyMaterial;
import com.example.aiplatform.learning.repository.LearningGoalRepository;
import com.example.aiplatform.learning.repository.StudyMaterialRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StudyMaterialService {

    private final LearningGoalRepository learningGoalRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final Path uploadRoot;

    public StudyMaterialService(
            LearningGoalRepository learningGoalRepository,
            StudyMaterialRepository studyMaterialRepository,
            @Value("${app.file-storage.upload-dir:storage/materials}") String uploadDir
    ) {
        this.learningGoalRepository = learningGoalRepository;
        this.studyMaterialRepository = studyMaterialRepository;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional
    public StudyMaterialResponse upload(UUID userId, UUID goalId, MultipartFile file) {
        LearningGoal goal = getOwnedGoal(userId, goalId);
        validateFile(file);

        String originalFileName = cleanFileName(file.getOriginalFilename());
        String contentType = resolveContentType(file);
        String extractedText = extractTxt(file);
        UUID materialId = UUID.randomUUID();
        Path materialDir = uploadRoot
                .resolve(userId.toString())
                .resolve(goalId.toString())
                .resolve(materialId.toString())
                .normalize();
        Path targetFile = materialDir.resolve("original.txt").normalize();

        if (!targetFile.startsWith(uploadRoot)) {
            throw new BadRequestException("Invalid file path");
        }

        try {
            Files.createDirectories(materialDir);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BadRequestException("Could not store uploaded material");
        }

        StudyMaterial material = studyMaterialRepository.save(new StudyMaterial(
                materialId,
                goal.getUser(),
                goal,
                originalFileName,
                contentType,
                file.getSize(),
                uploadRoot.relativize(targetFile).toString().replace('\\', '/'),
                extractedText
        ));

        return toResponse(material);
    }

    @Transactional(readOnly = true)
    public List<StudyMaterialResponse> getByGoal(UUID userId, UUID goalId) {
        if (!learningGoalRepository.existsByIdAndUserId(goalId, userId)) {
            throw new ResourceNotFoundException("Learning goal not found");
        }

        return studyMaterialRepository.findAllByLearningGoalIdAndUserIdOrderByCreatedAtDesc(goalId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyMaterialResponse getById(UUID userId, UUID materialId) {
        return studyMaterialRepository.findByIdAndUserId(materialId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Study material not found"));
    }

    private LearningGoal getOwnedGoal(UUID userId, UUID goalId) {
        return learningGoalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning goal not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        String fileName = cleanFileName(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        String contentType = resolveContentType(file).toLowerCase(Locale.ROOT);
        if (!fileName.endsWith(".txt") && !contentType.startsWith("text/plain")) {
            throw new BadRequestException("Only TXT materials are supported in the current release");
        }
    }

    private String cleanFileName(String fileName) {
        String cleaned = StringUtils.cleanPath(fileName == null ? "" : fileName).trim();
        if (cleaned.isBlank() || cleaned.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
        return cleaned;
    }

    private String resolveContentType(MultipartFile file) {
        return file.getContentType() == null || file.getContentType().isBlank()
                ? "text/plain"
                : file.getContentType();
    }

    private String extractTxt(MultipartFile file) {
        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8).trim();
            if (text.isBlank()) {
                throw new BadRequestException("Uploaded TXT material does not contain text");
            }
            return text;
        } catch (IOException exception) {
            throw new BadRequestException("Could not extract text from uploaded material");
        }
    }

    private StudyMaterialResponse toResponse(StudyMaterial material) {
        return new StudyMaterialResponse(
                material.getId(),
                material.getLearningGoal().getId(),
                material.getOriginalFileName(),
                material.getContentType(),
                material.getFileSize(),
                material.getProcessingStatus(),
                material.getExtractedText(),
                material.getCreatedAt()
        );
    }
}
