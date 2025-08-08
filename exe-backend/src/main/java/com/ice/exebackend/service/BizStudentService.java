package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.StudentExportDTO;
import com.ice.exebackend.entity.BizStudent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BizStudentService extends IService<BizStudent> {
    /**
     * 通过 Excel 批量导入学生
     * @param file Excel 文件
     * @param subjectId 关联的科目ID
     * @throws IOException
     */
    void importStudents(MultipartFile file, Long subjectId) throws IOException;

    /**
     * 【新增】根据条件查询用于导出的学生列表
     * @param subjectId 科目ID (可选)
     * @param name 学生姓名 (可选)
     * @return 用于导出的学生DTO列表
     */
    List<StudentExportDTO> getStudentExportList(Long subjectId, String name);
}