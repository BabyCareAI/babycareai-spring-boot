package babycareai.backend.domain.diagnosis.enums;

import lombok.Getter;

@Getter
public enum Symptoms {
    ITCHINESS("가려움증"),
    RED_SPOTS_RASH("붉은 반점/발진"),
    PIMPLES_BLISTERS("뾰루지/물집"),
    CRUSTS_SCALY_SKIN("딱지/비늘"),
    PAIN_SORENESS("통증/쓰라림"),
    FEVER_WARM_SKIN("열감/열"),
    SWELLING("붓기"),
    DISCHARGE_PUS("진물/고름"),
    EYE_MOUTH_SYMPTOMS("눈/입 관련 증상"),
    VOMITING_DIARRHEA("구토/설사"),
    LOSS_OF_APPETITE("식욕부진"),
    IRRITABILITY_CRYING("보챔/울음");

    private final String koreanName;

    Symptoms(String koreanName) {
        this.koreanName = koreanName;
    }

}