package com.test1.poc.controller;

import com.test1.poc.entity.Job;
import com.test1.poc.service.JobService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {
    private final JobService service;


    public JobController(JobService service) { this.service = service;}

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Job> getAllJobs(){

        return service.findAll();
    }
}
