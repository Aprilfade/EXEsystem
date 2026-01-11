package com.ice.exebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ice.exebackend.dto.ChapterTreeDTO;
import com.ice.exebackend.entity.BizCourseChapter;
import com.ice.exebackend.entity.BizCourseResource;
import com.ice.exebackend.mapper.BizCourseChapterMapper;
import com.ice.exebackend.mapper.BizCourseResourceMapper;
import com.ice.exebackend.service.CourseChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 课程章节服务实现
 */
@Service
public class CourseChapterServiceImpl extends ServiceImpl<BizCourseChapterMapper, BizCourseChapter> implements CourseChapterService {

    @Autowired
    private BizCourseResourceMapper resourceMapper;

    @Override
    public List<ChapterTreeDTO> getCourseChapterTree(Long courseId) {
        // 1. 获取所有章节
        List<BizCourseChapter> chapters = this.list(
            new QueryWrapper<BizCourseChapter>()
                .eq("course_id", courseId)
                .orderByAsc("sort_order")
        );

        // 2. 获取所有资源
        List<BizCourseResource> resources = resourceMapper.selectList(
            new QueryWrapper<BizCourseResource>()
                .eq("course_id", courseId)
                .orderByAsc("sort_order")
        );

        // 3. 递归构建树结构
        return buildTree(chapters, resources, 0L);
    }

    /**
     * 递归构建章节树
     */
    private List<ChapterTreeDTO> buildTree(
        List<BizCourseChapter> chapters,
        List<BizCourseResource> resources,
        Long parentId
    ) {
        return chapters.stream()
            .filter(c -> {
                Long pid = c.getParentId();
                return Objects.equals(pid != null ? pid : 0L, parentId);
            })
            .map(chapter -> {
                ChapterTreeDTO dto = new ChapterTreeDTO();
                dto.setId(chapter.getId());
                dto.setName(chapter.getName());
                dto.setDescription(chapter.getDescription());
                dto.setType("chapter");

                // 递归获取子章节
                List<ChapterTreeDTO> children = buildTree(chapters, resources, chapter.getId());
                dto.setChildren(children);

                // 添加该章节下的资源
                List<BizCourseResource> chapterResources = resources.stream()
                    .filter(r -> chapter.getId().equals(r.getChapterId()))
                    .collect(Collectors.toList());
                dto.setResources(chapterResources);

                return dto;
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean createChapter(BizCourseChapter chapter) {
        // 自动设置排序
        if (chapter.getSortOrder() == null) {
            Long count = this.count(
                new QueryWrapper<BizCourseChapter>()
                    .eq("course_id", chapter.getCourseId())
                    .eq("parent_id", chapter.getParentId() == null ? 0 : chapter.getParentId())
            );
            chapter.setSortOrder(count.intValue());
        }
        return this.save(chapter);
    }

    @Override
    @Transactional
    public boolean updateChapter(BizCourseChapter chapter) {
        return this.updateById(chapter);
    }

    @Override
    @Transactional
    public boolean deleteChapter(Long chapterId) {
        // 1. 删除子章节（递归）
        List<BizCourseChapter> children = this.list(
            new QueryWrapper<BizCourseChapter>().eq("parent_id", chapterId)
        );
        for (BizCourseChapter child : children) {
            deleteChapter(child.getId());
        }

        // 2. 将该章节下的资源的chapter_id设为null
        BizCourseResource resourceUpdate = new BizCourseResource();
        resourceUpdate.setChapterId(null);
        resourceMapper.update(resourceUpdate,
            new QueryWrapper<BizCourseResource>().eq("chapter_id", chapterId)
        );

        // 3. 删除章节本身
        return this.removeById(chapterId);
    }
}
