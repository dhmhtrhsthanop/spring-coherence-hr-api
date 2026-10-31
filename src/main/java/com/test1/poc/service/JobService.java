package com.test1.poc.service;

import com.test1.poc.entity.Job;
import com.test1.poc.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobService {
    private final JobRepository repository;

    public JobService(JobRepository repository) {
        this.repository = repository;
    }

    public List<Job> findAll() {

        return repository.findAll();
    }
}
