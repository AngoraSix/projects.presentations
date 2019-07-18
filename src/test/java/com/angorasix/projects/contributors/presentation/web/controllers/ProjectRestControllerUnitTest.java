package com.angorasix.projects.contributors.presentation.web.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class ProjectRestControllerUnitTest {

  @Test
  public void coverageTest() {
    ProjectRestController controller = new ProjectRestController();
    
    String output = controller.getAllProjects();

    assertThat(output).isEqualTo("TODO - projects");
  }

}
