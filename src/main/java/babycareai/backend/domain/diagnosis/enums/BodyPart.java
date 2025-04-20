package babycareai.backend.domain.diagnosis.enums;

import lombok.Getter;

@Getter
public enum BodyPart {
    FACE("얼굴"),
    SCALP("두피"),
    FOREHEAD("이마"),
    EYELIDS("눈꺼풀"),
    AROUND_EYES("눈 주위"),
    CHEEKS("뺨/볼"),
    NOSE_AREA("코 주변"),
    MOUTH_LIPS("입/입술"),
    EARS("귀"),
    NECK("목"),
    CHEST("가슴"),
    TUMMY("배"),
    BACK("등"),
    ARMPITS("겨드랑이"),
    ARMS_HANDS("팔/손"),
    LEGS_FEET("다리/발"),
    BOTTOM("엉덩이"),
    GENITAL_AREA("생식기 부위"),
    NAPPY_AREA("기저귀 부위"),
    BEHIND_KNEES("무릎 뒤"),
    INNER_ELBOWS("팔꿈치 안쪽"),
    WHOLE_BODY("전신");

    private final String koreanName;

    BodyPart(String koreanName) {
        this.koreanName = koreanName;
    }

}