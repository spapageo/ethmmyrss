package com.spapageo.ethmmyrss.resourses;

import com.google.common.base.MoreObjects;
import io.dropwizard.views.View;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toList;

class IndexView extends View {

    private final List<LessonInfo> lessonInfos;

    IndexView(Map<Integer, String> lessons) {
        super("index.mustache", StandardCharsets.UTF_8);
        lessonInfos = lessons.entrySet().stream()
                .map(entry -> new LessonInfo(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(LessonInfo::getLessonName))
                .collect(toList());
    }

    @SuppressWarnings("unused")
    List<LessonInfo> getLessonInfos() {
        return lessonInfos;
    }

    private static final class LessonInfo {
        private final int lessonId;
        private final String lessonName;

        private LessonInfo(int lessonId, String lessonName) {
            this.lessonId = lessonId;
            this.lessonName = lessonName;
        }

        @SuppressWarnings("unused")
        int getLessonId() {
            return lessonId;
        }

        @SuppressWarnings("unused")
        String getLessonName() {
            return lessonName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LessonInfo that = (LessonInfo) o;
            return lessonId == that.lessonId &&
                    Objects.equals(lessonName, that.lessonName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lessonId, lessonName);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("lessonId", lessonId)
                    .add("lessonName", lessonName)
                    .toString();
        }
    }
}
