package com.nungil.domain.task;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public TaskVO searchByKeyword(String keyword) {
        List<TaskVO> results = taskMapper.findByKeyword(keyword);
        return results.isEmpty() ? null : results.get(0);
    }

    public TaskVO findById(Long taskId) {
        return taskMapper.findById(taskId);
    }

    public List<TaskVO> findAll() {
        return taskMapper.findAll();
    }
}
