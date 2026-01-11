package com.ice.exebackend.controller.admin;

import com.ice.exebackend.common.Result;
import com.ice.exebackend.dto.ChapterTreeDTO;
import com.ice.exebackend.entity.BizCourseChapter;
import com.ice.exebackend.service.CourseChapterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端课程章节控制器
 * 教师/管理员专用API
 */
@RestController
@RequestMapping("/api/v1/admin/course-chapter")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
public class CourseChapterAdminController {

    private static final Logger logger = LoggerFactory.getLogger(CourseChapterAdminController.class);

    @Autowired
    private CourseChapterService chapterService;

    /**
     * 获取课程章节树（管理员视图，含资源）
     *
     * @param courseId 课程ID
     * @return 章节树
     */
    @GetMapping("/course/{courseId}/tree")
    public Result getChapterTree(@PathVariable Long courseId) {
        logger.info("管理员获取课程章节树: courseId={}", courseId);

        try {
            List<ChapterTreeDTO> tree = chapterService.getCourseChapterTree(courseId);
            return Result.suc(tree);
        } catch (Exception e) {
            logger.error("获取章节树失败", e);
            return Result.fail("获取章节树失败: " + e.getMessage());
        }
    }

    /**
     * 创建章节
     *
     * @param chapter 章节信息
     * @return 创建的章节ID
     */
    @PostMapping
    public Result createChapter(@RequestBody BizCourseChapter chapter) {
        logger.info("创建章节: courseId={}, name={}, parentId={}",
            chapter.getCourseId(), chapter.getName(), chapter.getParentId());

        try {
            boolean success = chapterService.createChapter(chapter);
            if (success) {
                return Result.suc(chapter.getId());
            } else {
                return Result.fail("章节创建失败");
            }
        } catch (Exception e) {
            logger.error("创建章节失败", e);
            return Result.fail("创建章节失败: " + e.getMessage());
        }
    }

    /**
     * 更新章节
     *
     * @param id      章节ID
     * @param chapter 章节信息
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result updateChapter(@PathVariable Long id, @RequestBody BizCourseChapter chapter) {
        logger.info("更新章节: id={}, name={}", id, chapter.getName());

        try {
            chapter.setId(id);
            boolean success = chapterService.updateChapter(chapter);
            if (success) {
                return Result.suc("章节更新成功");
            } else {
                return Result.fail("章节更新失败");
            }
        } catch (Exception e) {
            logger.error("更新章节失败", e);
            return Result.fail("更新章节失败: " + e.getMessage());
        }
    }

    /**
     * 删除章节（级联删除子章节，资源chapter_id置null）
     *
     * @param id 章节ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteChapter(@PathVariable Long id) {
        logger.info("删除章节: id={}", id);

        try {
            boolean success = chapterService.deleteChapter(id);
            if (success) {
                return Result.suc("章节删除成功");
            } else {
                return Result.fail("章节删除失败");
            }
        } catch (Exception e) {
            logger.error("删除章节失败", e);
            return Result.fail("删除章节失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新章节排序
     *
     * @param sortList 排序列表 [{id: 1, sortOrder: 0, parentId: 0}, ...]
     * @return 操作结果
     */
    @PostMapping("/batch-sort")
    public Result batchUpdateSort(@RequestBody List<Map<String, Object>> sortList) {
        logger.info("批量更新章节排序: count={}", sortList.size());

        try {
            for (Map<String, Object> item : sortList) {
                Long id = Long.valueOf(item.get("id").toString());
                Integer sortOrder = Integer.valueOf(item.get("sortOrder").toString());
                Long parentId = item.get("parentId") != null ?
                    Long.valueOf(item.get("parentId").toString()) : 0L;

                BizCourseChapter chapter = new BizCourseChapter();
                chapter.setId(id);
                chapter.setSortOrder(sortOrder);
                chapter.setParentId(parentId);

                chapterService.updateChapter(chapter);
            }

            return Result.suc("排序更新成功");
        } catch (Exception e) {
            logger.error("批量更新排序失败", e);
            return Result.fail("批量更新排序失败: " + e.getMessage());
        }
    }

    /**
     * 移动章节（修改parent_id）
     *
     * @param id          章节ID
     * @param newParentId 新的父章节ID
     * @return 操作结果
     */
    @PostMapping("/{id}/move")
    public Result moveChapter(@PathVariable Long id, @RequestParam Long newParentId) {
        logger.info("移动章节: id={}, newParentId={}", id, newParentId);

        try {
            BizCourseChapter chapter = new BizCourseChapter();
            chapter.setId(id);
            chapter.setParentId(newParentId);

            boolean success = chapterService.updateChapter(chapter);
            if (success) {
                return Result.suc("章节移动成功");
            } else {
                return Result.fail("章节移动失败");
            }
        } catch (Exception e) {
            logger.error("移动章节失败", e);
            return Result.fail("移动章节失败: " + e.getMessage());
        }
    }
}
