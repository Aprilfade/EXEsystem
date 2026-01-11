package com.ice.exebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ice.exebackend.dto.ChapterTreeDTO;
import com.ice.exebackend.entity.BizCourseChapter;

import java.util.List;

/**
 * 课程章节服务接口
 */
public interface CourseChapterService extends IService<BizCourseChapter> {

    /**
     * 获取课程章节树（含资源）
     * @param courseId 课程ID
     * @return 章节树列表
     */
    List<ChapterTreeDTO> getCourseChapterTree(Long courseId);

    /**
     * 创建章节
     * @param chapter 章节信息
     * @return 是否成功
     */
    boolean createChapter(BizCourseChapter chapter);

    /**
     * 更新章节
     * @param chapter 章节信息
     * @return 是否成功
     */
    boolean updateChapter(BizCourseChapter chapter);

    /**
     * 删除章节（级联删除子章节和资源关联）
     * @param chapterId 章节ID
     * @return 是否成功
     */
    boolean deleteChapter(Long chapterId);
}
