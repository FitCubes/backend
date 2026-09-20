package fitcubes.service.diary;

import fitcubes.dto.diary.DiaryResponseDto;
import java.time.LocalDate;

public interface DiaryService {

    DiaryResponseDto getDiary(Long userId, LocalDate date);
}
