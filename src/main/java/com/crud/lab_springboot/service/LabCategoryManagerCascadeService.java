package com.crud.lab_springboot.service;

import com.crud.lab_springboot.projo.Lab;
import com.crud.lab_springboot.projo.User;
import com.crud.lab_springboot.reposity.LabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 实验室分类管理员变更时，对下属实验室的 manager_id 做级联更新（与 {@code lab.manager_id} 对齐）。
 * 须在已有事务中调用（由 Controller 的 {@code @Transactional} 包裹），以保证分类与实验室同事务提交或回滚。
 */
@Service
public class LabCategoryManagerCascadeService {

    @Autowired
    private LabRepository labRepository;

    /**
     * 将该分类下所有实验室的实验室管理员更新为 {@code manager}，并批量持久化。
     *
     * @param categoryId 分类主键
     * @param manager    新的实验室管理员用户（与 lab_category.manager_id 指向同一用户）
     */
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void cascadeManagerToLabsInCategory(Long categoryId, User manager) {
        List<Lab> labs = labRepository.findByCategory_Id(categoryId);
        if (labs.isEmpty()) {
            return;
        }
        for (Lab lab : labs) {
            lab.setManager(manager);
        }
        labRepository.saveAll(labs);
    }
}
