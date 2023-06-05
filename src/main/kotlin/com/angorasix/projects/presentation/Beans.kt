package com.angorasix.projects.presentation

import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.projects.presentation.presentation.handler.ProjectsPresentationHandler
import com.angorasix.projects.presentation.presentation.router.ProjectsPresentationRouter
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

val beans = beans {
    bean<ProjectsPresentationService>()
    bean<ProjectsPresentationHandler>()
    bean {
        ProjectsPresentationRouter(ref(), ref()).projectRouterFunction()
    }
}

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) = beans.initialize(context)
}
