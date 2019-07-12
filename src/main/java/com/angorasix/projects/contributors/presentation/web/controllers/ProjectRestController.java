package com.angorasix.projects.contributors.presentation.web.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestController containing Project endpoints.
 * 
 * @author rozagerardo
 *
 */
@RestController
@RequestMapping("projects")
public class ProjectRestController {

  /**
   * Get all projects - test.
   * 
   * @return hardcoded string just for test
   */
  @GetMapping
  public String getAllProjects() {
    return "TODO - projects";
  }

}
