package com.aws.utils;

import lombok.Getter;

import java.util.Arrays;

public class Constant {

    /**
     * Enum đại diện nhóm loại file chính
     */
    public enum FileCategory {
        IMAGES,
        VIDEOS,
        DOCUMENTS,
        OTHER
    }

    /**
     * Enum ánh xạ mimeType → category
     */
    @Getter
    public enum MimeTypeEnum {
        // IMAGE
        JPG("image/jpeg", FileCategory.IMAGES),
        PNG("image/png", FileCategory.IMAGES),
        GIF("image/gif", FileCategory.IMAGES),
        WEBP("image/webp", FileCategory.IMAGES),

        // VIDEO
        MP4("video/mp4", FileCategory.VIDEOS),
        MOV("video/quicktime", FileCategory.VIDEOS),
        AVI("video/x-msvideo", FileCategory.VIDEOS),

        // DOCUMENT
        PDF("application/pdf", FileCategory.DOCUMENTS),
        DOC("application/msword", FileCategory.DOCUMENTS),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", FileCategory.DOCUMENTS),
        XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FileCategory.DOCUMENTS),
        PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", FileCategory.DOCUMENTS),
        TXT("text/plain", FileCategory.DOCUMENTS),

        // UNKNOWN
        UNKNOWN("application/octet-stream", FileCategory.OTHER);

        private final String mimeType;
        private final FileCategory category;

        MimeTypeEnum(String mimeType, FileCategory category) {
            this.mimeType = mimeType;
            this.category = category;
        }

        public static FileCategory classify(String mimeType) {
            return Arrays.stream(MimeTypeEnum.values())
                    .filter(e -> e.mimeType.equalsIgnoreCase(mimeType))
                    .findFirst()
                    .map(MimeTypeEnum::getCategory)
                    .orElse(FileCategory.OTHER);
        }
    }
}