// src/main/java/com/inview/backend/repository/ResumeRepository.java
package com.inview.backend.repository;

import com.inview.backend.entity.Resume;
import com.inview.backend.dto.ResumeListDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 목록 조회는 DTO 프로젝션(파일데이터 미로딩)
    @Query("""
           select new com.inview.backend.dto.ResumeListDto(
               r.id, r.fileName, r.contentType, r.fileSize, r.createdAt
           )
           from Resume r
           where r.userId = :userId
           order by r.createdAt desc
           """)
    List<ResumeListDto> findListByUserId(@Param("userId") String userId);
}
