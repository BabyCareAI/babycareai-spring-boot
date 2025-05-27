package babycareai.backend.domain.diagnosis.enums;

import lombok.Getter;

@Getter
public enum BodyPart {
    ARMPITS("겨드랑이"),
    ARMS("팔"),
    BACK("등"),
    BACKS_OF_KNEES("오금"),
    BETWEEN_TOES("발가락 사이"),
    BOTTOM("엉덩이"),
    CHEEKS("뺨"),
    CHEST("가슴"),
    CHIN("턱"),
    EAR("귀"),
    ELBOWS("팔꿈치"),
    EYES("눈"),
    EYELIDS("눈꺼풀"),
    FACE("얼굴"),
    FEET("발"),
    FOREHEAD("이마"),
    GENITALS("생식기"),
    GUMS("잇몸"),
    HAIRLINE("헤어라인"),
    HANDS("손"),
    HEAD("머리"),
    INNER_THIGHS("허벅지 안쪽"),
    INSIDE_THE_MOUTH("입안"),
    KNEECAPS("무릎뼈"),
    KNEES("무릎"),
    LEGS("다리"),
    LIPS("입술"),
    MOUTH("입"),
    NAPPY_AREA("기저귀 부위"),
    NECK("목"),
    NOSE("코"),
    PALMS("손바닥"),
    ROOF_OF_THE_MOUTH("입천장"),
    SCALP("두피"),
    SKIN_AROUND_THE_BITE("물린 부위 주변 피부"),
    SKIN_FOLDS("피부 주름"),
    SOLES("발바닥"),
    THROAT("목구멍"),
    THIGHS("허벅지"),
    TOES("발가락"),
    TONGUE("혀"),
    TORSO("몸통"),
    TUMMY("배"),
    WHOLE_BODY("전신"),
    WRISTS("손목");

    private final String koreanName;

    BodyPart(String koreanName) {
        this.koreanName = koreanName;
    }

}